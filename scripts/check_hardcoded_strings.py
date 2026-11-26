#!/usr/bin/env python3
"""Invariant 6 gate: fail when a Compose screen passes hardcoded text to the UI.

Scans presentation-layer Kotlin under app/src/main for string literals given to
user-visible parameters (Text(...), text =, label =, contentDescription =, ...).
Skipped: @Preview functions, top-level `preview*` sample values, and `label =`
debug names on Compose animations. Literals without letters (such as "•" or
"") are allowed. Exit code 1 lists every offending line.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
SOURCE_DIR = ROOT / "app/src/main/java/com/fundcircle/app/presentation"

UI_ARGUMENT = re.compile(
    r'(?:\bText\(\s*|\b(?:text|label|title|placeholder|contentDescription|supportingText|message)\s*=\s*)'
    r'"((?:[^"\\]|\\.)*)"'
)
PREVIEW_VALUE = re.compile(r'^(private\s+|internal\s+)?val\s+preview\w*\b')
ANIMATION_CALL = re.compile(r'\banimate\w*\(|\bupdateTransition\(|\bAnimatedContent\(|\bCrossfade\(|\brememberInfiniteTransition\(|\banimate\w*AsState\(')
ANIMATION_LOOKBACK_LINES = 6
TEMPLATE = re.compile(r'\$\{[^}]*\}|\$[A-Za-z_][A-Za-z0-9_]*')
LETTER = re.compile(r'[^\W\d_]')


def is_user_text(literal: str) -> bool:
    """True when the literal still holds words once template expressions are removed."""
    return bool(LETTER.search(TEMPLATE.sub("", literal)))


def is_animation_label(lines: list[str], index: int) -> bool:
    """True when a `label =` argument belongs to a Compose animation call (a debug name, never shown)."""
    window = lines[max(0, index - ANIMATION_LOOKBACK_LINES):index + 1]
    return any(ANIMATION_CALL.search(candidate) for candidate in window)


def bracket_balance(line: str) -> int:
    """Net open braces and parentheses on a line (string contents are not special-cased)."""
    return line.count("{") + line.count("(") - line.count("}") - line.count(")")


def scan(path: pathlib.Path) -> list[str]:
    problems = []
    lines = path.read_text(encoding="utf-8").splitlines()
    awaiting_preview_fun = False
    skip_depth = None  # bracket depth of the preview block being skipped, or None

    for number, line in enumerate(lines, start=1):
        stripped = line.strip()

        if skip_depth is None:
            if stripped.startswith("@Preview"):
                awaiting_preview_fun = True
            starts_preview_fun = awaiting_preview_fun and re.match(r'(private\s+|internal\s+)?fun\s', stripped)
            if starts_preview_fun or PREVIEW_VALUE.match(line):
                awaiting_preview_fun = False
                skip_depth = 0

        if skip_depth is not None:
            skip_depth += bracket_balance(line)
            # The block ends once its brackets balance, unless the line only opens it (`fun X() =`).
            if skip_depth <= 0 and not line.rstrip().endswith(("=", "{", "(")):
                skip_depth = None
            continue

        if stripped.startswith("//") or stripped.startswith("*"):
            continue
        for match in UI_ARGUMENT.finditer(line):
            if match.group(0).lstrip().startswith("label") and is_animation_label(lines, number - 1):
                continue
            if is_user_text(match.group(1)):
                problems.append(f"{path.relative_to(ROOT)}:{number}: {stripped}")
                break  # one report per line is enough
    return problems


def main() -> int:
    problems = [p for f in sorted(SOURCE_DIR.rglob("*.kt")) for p in scan(f)]
    if problems:
        print("::error::Invariant 6 violation: hardcoded user-facing text. Move it to res/values/strings.xml.")
        print("\n".join(problems))
        return 1
    print("No hardcoded user-facing strings found in presentation code.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
