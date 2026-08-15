package app.dyrecto.capability

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins the camera-agnostic capability foundation: raw-record → CameraProperty mapping (incl.
 * forward-compat preservation of unknown codes), feature resolution, control validation, and
 * report serialization. No transport involved — this is why the mapping lives in commonMain.
 */
class CapabilityModelTest {

    private fun record(
        code: Int,
        getSet: Int = 1,
        avail: Int = 1,
        current: Long? = 100L,
        form: RawForm = RawForm.None,
        dataType: Int = 0x0004,
    ) = RawPropertyRecord(code, dataType, getSet, avail, current, null, form)

    // ---- mapping ----

    @Test
    fun mapsWritableAvailableProperty() {
        val p = CapabilityMapper.toProperty(record(0xD21E, getSet = 1, avail = 1))
        assertTrue(p.writable)
        assertTrue(p.available)
        assertTrue(p.known)
        assertEquals("ISO Sensitivity", p.label)
    }

    @Test
    fun readOnlyAndUnavailableFlags() {
        val ro = CapabilityMapper.toProperty(record(0xD218, getSet = 0, avail = 1))
        assertFalse(ro.writable)
        val na = CapabilityMapper.toProperty(record(0xD21E, getSet = 1, avail = 0))
        assertFalse(na.available)
    }

    @Test
    fun unknownCodeIsPreservedNotDiscarded() {
        val p = CapabilityMapper.toProperty(record(0x9ABC))
        assertFalse(p.known)
        assertEquals("0x9ABC", p.label)
        assertEquals(0x9ABC, p.code) // raw info retained
    }

    @Test
    fun rangeAndEnumFormsCarryLegalValues() {
        val r = CapabilityMapper.toProperty(record(0xD21E, form = RawForm.Range(100, 3200, 100)))
        assertTrue((r.valueSet as PropertyValueSet.Range).contains(800))
        assertFalse((r.valueSet as PropertyValueSet.Range).contains(850))
        val e = CapabilityMapper.toProperty(record(0x5007, form = RawForm.Enum(listOf(280, 400, 560))))
        assertTrue((e.valueSet as PropertyValueSet.Enum).contains(400))
        assertFalse((e.valueSet as PropertyValueSet.Enum).contains(500))
    }

    // ---- feature resolution ----

    @Test
    fun featuresResolveFromKnownProperties() {
        val caps = CapabilityMapper.build(
            CameraInfo(model = "ILME-FX3", transport = CameraTransport.WIFI),
            listOf(
                record(0xD221), // live view
                record(0xD21E, getSet = 1), // ISO writable
                record(0x5007, getSet = 1), // FNumber writable
                record(0xD060), // subject AF
                record(0xD21D), // movie rec
            ),
        )
        assertTrue(caps.supports(CameraFeature.LIVE_VIEW))
        assertTrue(caps.supports(CameraFeature.WAVEFORM)) // phone-side, follows live view
        assertTrue(caps.supports(CameraFeature.ISO_CONTROL))
        assertTrue(caps.supports(CameraFeature.IRIS_CONTROL))
        assertTrue(caps.supports(CameraFeature.EYE_AF))
        assertTrue(caps.supports(CameraFeature.RECORD_CONTROL))
        assertFalse(caps.supports(CameraFeature.USB))
    }

    @Test
    fun readOnlyIsoDoesNotYieldControlFeature() {
        val caps = CapabilityMapper.build(
            CameraInfo(),
            listOf(record(0xD21E, getSet = 0)),
        )
        assertFalse(caps.supports(CameraFeature.ISO_CONTROL))
    }

    // ---- control validation ----

    @Test
    fun validatorRejectsUnknownNonWritableUnavailableAndOutOfRange() {
        val caps = CapabilityMapper.build(
            CameraInfo(),
            listOf(
                record(0xD21E, getSet = 1, avail = 1, form = RawForm.Range(100, 3200, 100)),
                record(0xD218, getSet = 0, avail = 1), // read-only
                record(0x5007, getSet = 1, avail = 0), // unavailable
            ),
        )
        assertNull(CameraControlValidator.validate(caps, 0xD21E, 800))
        assertEquals(ControlRejection.OUT_OF_RANGE, CameraControlValidator.validate(caps, 0xD21E, 850))
        assertEquals(ControlRejection.NOT_WRITABLE, CameraControlValidator.validate(caps, 0xD218, 1))
        assertEquals(ControlRejection.NOT_AVAILABLE, CameraControlValidator.validate(caps, 0x5007, 400))
        assertEquals(ControlRejection.UNKNOWN_PROPERTY, CameraControlValidator.validate(caps, 0x1234, 1))
    }

    // ---- report ----

    @Test
    fun reportRoundTripsJsonAndPreservesUnknowns() {
        val caps = CapabilityMapper.build(
            CameraInfo(manufacturer = "Sony", model = "ILME-FX3", transport = CameraTransport.WIFI),
            listOf(record(0xD21E, getSet = 1), record(0x9ABC)),
        )
        val report = CapabilityReporter.build(caps, generatedAtMs = 1_700_000_000_000L)
        assertEquals(CapabilitySchema.VERSION, report.schemaVersion)
        assertTrue(report.unknownPropertyCodes.contains(0x9ABC))
        val jsonText = CapabilityReporter.toJson(report)
        assertTrue(jsonText.contains("ILME-FX3"))
        assertTrue(jsonText.contains("0x9ABC") || jsonText.contains("39612")) // label or code present
        val md = CapabilityReporter.toMarkdown(report)
        assertTrue(md.contains("# Camera Capability Report"))
        assertTrue(md.contains("ISO Sensitivity"))
    }
}
