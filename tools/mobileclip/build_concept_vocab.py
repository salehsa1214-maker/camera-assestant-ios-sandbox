#!/usr/bin/env python3
"""Phase 16.2 — precompute the DISCRIMINATIVE creative-concept vocabulary (MobileCLIP2-S0 TEXT tower).

Offline, run once. For each curated concept prompt we encode the text, L2-normalize, and write the
vector into the JSON schema `ConceptVocabulary.fromJson` expects. On device we score the reference
IMAGE embedding against these vectors (cosine) — so the text tower never ships.

Phase 16.2 redesign: the vocabulary is no longer a set of scene CATEGORIES (portrait / warm /
close-up). Those merely classify. Instead it is a set of CONTRASTIVE, RELATIONAL axes — each a pair of
opposing poles (same `family`) — chosen to *discriminate* between visually similar references:
isolation↔clutter, edge-tension↔centered, layered-depth↔flat, directional-dramatic-light↔flat-light,
rim/silhouette separation, minimal↔dense, one-anchor↔spread-attention, symmetry↔tension,
diagonal-energy↔stillness, intimate↔environmental, saturated↔muted, tense↔serene. Several probe
attributes the deterministic experts CANNOT measure (true depth of field, saturation, light quality),
so semantic evidence complements — never replaces — the measured signals.

`aspect` links a concept to a `CreativeAspect` enum name (uppercase) so `SemanticExpert` routes it to
a bounded importance hint. `family` groups the two opposing poles of an axis. The coupling lives in
this DATA — swapping the vocabulary changes behavior without touching device code.
"""
import argparse
import json

import torch


MODEL_ID = "mobileclip2_s0_image"  # must match the image tower's model id used on device

# (label, aspect|None, family, prompt). Contrastive pairs share a `family`; prompts are descriptive,
# not category names, so cosine separates similar shots along each discriminative axis.
CONCEPTS = [
    # Subject isolation vs. cluttered surroundings
    ("isolated_clean_subject", "BACKGROUND", "isolation", "a subject cleanly isolated against an empty, uncluttered background"),
    ("cluttered_busy_scene", "BACKGROUND", "isolation", "a subject embedded in a busy, cluttered, detailed environment"),
    # Edge tension vs. centered breathing room
    ("subject_pushed_to_edge", "SUBJECT_PLACEMENT", "edge_tension", "a subject pushed hard against the edge of the frame"),
    ("subject_centered_calm", "SUBJECT_PLACEMENT", "edge_tension", "a subject centered with even breathing room on all sides"),
    # Layered depth vs. flat single plane
    ("layered_depth", "DEPTH_OF_FIELD", "depth_layers", "a photo with deep layers of foreground, midground and background"),
    ("flat_single_plane", "DEPTH_OF_FIELD", "depth_layers", "a flat photo with everything on a single plane"),
    # Shallow-focus separation vs. deep focus
    ("shallow_focus_separation", "DEPTH_OF_FIELD", "focus_depth", "a sharp subject separated from a soft, blurred background"),
    ("deep_focus_sharp", "DEPTH_OF_FIELD", "focus_depth", "a photo sharp from foreground to background"),
    # Directional dramatic light vs. flat even light
    ("dramatic_directional_light", "LIGHTING", "light_quality", "a subject carved by dramatic directional light and deep shadows"),
    ("flat_even_light", "LIGHTING", "light_quality", "a subject under flat, even, shadowless light"),
    # Backlit rim / silhouette separation
    ("backlit_rim_separation", "LIGHTING", "light_separation", "a subject rim-lit and separated from a bright background"),
    ("silhouette_against_light", "LIGHTING", "light_separation", "a dark silhouette against a bright, glowing background"),
    # Minimal empty space vs. dense detail
    ("minimalist_empty_space", "NEGATIVE_SPACE", "density", "a minimalist composition dominated by empty negative space"),
    ("dense_edge_to_edge_detail", "NEGATIVE_SPACE", "density", "a dense composition packed with detail from edge to edge"),
    # One dominant anchor vs. spread attention
    ("single_dominant_anchor", "COMPOSITION", "focal_anchor", "a photo with one dominant focal point that pulls the eye"),
    ("attention_spread_evenly", "COMPOSITION", "focal_anchor", "a photo with attention spread evenly across many elements"),
    # Symmetry / balance vs. asymmetric tension
    ("symmetrical_balanced", "COMPOSITION", "balance", "a symmetrical, deliberately balanced composition"),
    ("asymmetric_visual_tension", "COMPOSITION", "balance", "an asymmetric composition with deliberate visual tension"),
    # Diagonal energy vs. horizontal stillness
    ("diagonal_dynamic_energy", "COMPOSITION", "line_energy", "a dynamic composition built on strong diagonal lines"),
    ("horizontal_calm_stillness", "COMPOSITION", "line_energy", "a calm composition of horizontal lines and stillness"),
    # Intimate tight frame vs. distant environmental
    ("intimate_tight_frame", "SUBJECT_SCALE", "framing_distance", "an intimate, tightly framed shot filling the frame with the subject"),
    ("distant_environmental", "SUBJECT_SCALE", "framing_distance", "a distant subject small within a wide environment"),
    # Bold saturated vs. muted palette (saturation is NOT measured deterministically)
    ("bold_saturated_color", "COLOR", "saturation", "a photo with bold, vivid, saturated colors"),
    ("muted_desaturated_color", "COLOR", "saturation", "a photo with muted, desaturated, subtle colors"),
    # Tense / dramatic vs. calm / serene mood
    ("tense_dramatic_mood", "MOOD", "energy", "a tense, dramatic, high-energy photograph"),
    ("calm_serene_mood", "MOOD", "energy", "a calm, serene, quiet photograph"),
]


HF_NAME = "hf-hub:timm/MobileCLIP2-S0-OpenCLIP"


def load_text_encoder():
    import open_clip
    model, _, _ = open_clip.create_model_and_transforms(HF_NAME)
    model.eval()
    tokenizer = open_clip.get_tokenizer(HF_NAME)
    return model, tokenizer


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--out", required=True)
    args = ap.parse_args()

    model, tokenizer = load_text_encoder()
    entries = []
    dim = None
    with torch.no_grad():
        for label, aspect, family, prompt in CONCEPTS:
            tokens = tokenizer([prompt])
            feats = model.encode_text(tokens)
            feats = torch.nn.functional.normalize(feats, dim=-1)[0]
            vec = feats.tolist()
            dim = len(vec)
            entry = {"label": label, "family": family, "vector": vec}
            if aspect:
                entry["aspect"] = aspect
            entries.append(entry)

    out = {"modelId": MODEL_ID, "dimensions": dim, "concepts": entries}
    with open(args.out, "w", encoding="utf-8") as f:
        json.dump(out, f)
    print(f"wrote {args.out}: {len(entries)} concepts, dim={dim}")


if __name__ == "__main__":
    main()
