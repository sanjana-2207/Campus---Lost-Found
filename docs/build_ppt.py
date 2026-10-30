#!/usr/bin/env python3
"""
Build the presentation for the Campus Lost & Found project.

    pip install python-pptx
    python docs/build_ppt.py

Output: docs/Campus_Lost_and_Found_Presentation.pptx  (16:9, 30 slides, speaker notes)
"""

import os

from pptx import Presentation
from pptx.dml.color import RGBColor
from pptx.enum.dml import MSO_LINE_DASH_STYLE
from pptx.enum.shapes import MSO_CONNECTOR, MSO_SHAPE
from pptx.enum.text import MSO_ANCHOR, PP_ALIGN
from pptx.oxml.ns import qn
from pptx.util import Inches, Pt

# ----------------------------------------------------------------------------
#  Palette (identical to the application theme in UIStyle.java)
# ----------------------------------------------------------------------------
NAVY = RGBColor(0x13, 0x2A, 0x3E)
NAVY_SOFT = RGBColor(0x1D, 0x3D, 0x56)
ACCENT = RGBColor(0x2C, 0x7D, 0xA0)
ACCENT_DARK = RGBColor(0x1F, 0x5C, 0x78)
ACCENT_LIGHT = RGBColor(0xE7, 0xF1, 0xF7)
BG = RGBColor(0xF2, 0xF5, 0xF8)
WHITE = RGBColor(0xFF, 0xFF, 0xFF)
SOFT = RGBColor(0xF7, 0xF9, 0xFB)
BORDER = RGBColor(0xDC, 0xE3, 0xEA)
TEXT = RGBColor(0x1F, 0x2A, 0x37)
MUTED = RGBColor(0x6B, 0x7A, 0x8C)
SUCCESS = RGBColor(0x2E, 0x7D, 0x4F)
WARNING = RGBColor(0xC7, 0x7D, 0x00)
DANGER = RGBColor(0xC0, 0x39, 0x2B)
INK = RGBColor(0x0F, 0x22, 0x33)
CODE = RGBColor(0xD6, 0xE4, 0xEF)
CODE_KEY = RGBColor(0x7F, 0xA6, 0xBF)
TEAL = RGBColor(0x8F, 0xD3, 0xDE)

W, H = 13.333, 7.5
M = 0.72
CW = W - 2 * M

prs = Presentation()
prs.slide_width = Inches(W)
prs.slide_height = Inches(H)


# ----------------------------------------------------------------------------
#  Primitives
# ----------------------------------------------------------------------------
def new_slide(background=BG):
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    fill = slide.background.fill
    fill.solid()
    fill.fore_color.rgb = background
    return slide


def box(slide, x, y, w, h, fill=WHITE, line=BORDER, radius=0.05, shape=MSO_SHAPE.ROUNDED_RECTANGLE):
    shp = slide.shapes.add_shape(shape, Inches(x), Inches(y), Inches(w), Inches(h))
    if fill is None:
        shp.fill.background()
    else:
        shp.fill.solid()
        shp.fill.fore_color.rgb = fill
    if line is None:
        shp.line.fill.background()
    else:
        shp.line.color.rgb = line
        shp.line.width = Pt(1)
    shp.shadow.inherit = False
    if shape == MSO_SHAPE.ROUNDED_RECTANGLE:
        try:
            shp.adjustments[0] = radius
        except Exception:
            pass
    if shp.has_text_frame:
        shp.text_frame.text = ""
    return shp


def add_rich(paragraph, value, size, color, bold=False, font="Segoe UI", italic=False):
    """Adds text to a paragraph, supporting **bold** segments.

    Backticks are stripped: they read as code in Markdown source but look like
    literal noise on a slide.
    """
    parts = str(value).replace("`", "").split("**")
    for index, part in enumerate(parts):
        if not part:
            continue
        run = paragraph.add_run()
        run.text = part
        f = run.font
        f.size = Pt(size)
        f.name = font
        f.bold = bold or (index % 2 == 1)
        f.italic = italic
        f.color.rgb = color


def text(slide, x, y, w, h, value, size=16, bold=False, color=TEXT,
         font="Segoe UI", align=PP_ALIGN.LEFT, anchor=MSO_ANCHOR.TOP,
         spacing=1.05, italic=False):
    tb = slide.shapes.add_textbox(Inches(x), Inches(y), Inches(w), Inches(h))
    tf = tb.text_frame
    tf.word_wrap = True
    tf.margin_left = 0
    tf.margin_right = 0
    tf.margin_top = 0
    tf.margin_bottom = 0
    tf.vertical_anchor = anchor
    for i, line in enumerate(str(value).split("\n")):
        p = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
        p.alignment = align
        p.line_spacing = spacing
        add_rich(p, line, size, color, bold, font, italic)
    return tb


def bullets(slide, x, y, w, items, size=15, color=TEXT, gap=9,
            bullet_color=ACCENT, line_spacing=1.05):
    tb = slide.shapes.add_textbox(Inches(x), Inches(y), Inches(w), Inches(0.4))
    tf = tb.text_frame
    tf.word_wrap = True
    tf.margin_left = 0
    tf.margin_right = 0
    tf.margin_top = 0
    tf.margin_bottom = 0

    for i, item in enumerate(items):
        sub = isinstance(item, tuple)
        label = item[0] if sub else item
        p = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
        p.space_after = Pt(gap)
        p.line_spacing = line_spacing
        if sub:
            p.level = 1
        marker = p.add_run()
        marker.text = "–  " if sub else "•  "
        marker.font.size = Pt(size - (1 if sub else 0))
        marker.font.bold = True
        marker.font.name = "Segoe UI"
        marker.font.color.rgb = bullet_color if not sub else MUTED
        add_rich(p, label, size - (1 if sub else 0), color)
    return tb


def chrome(slide, title, subtitle, number, content_top=1.68):
    slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(M), Inches(0.58),
                           Inches(0.07), Inches(0.62)).fill.solid()
    slide.shapes[-1].fill.fore_color.rgb = ACCENT
    slide.shapes[-1].line.fill.background()
    text(slide, M + 0.22, 0.52, CW - 0.22, 0.55, title, 26, True, NAVY)
    if subtitle:
        text(slide, M + 0.23, 1.13, CW - 0.23, 0.38, subtitle, 11.5, False, MUTED)
    line = slide.shapes.add_connector(MSO_CONNECTOR.STRAIGHT, Inches(M), Inches(6.96),
                                      Inches(W - M), Inches(6.96))
    line.line.color.rgb = BORDER
    line.line.width = Pt(1)
    text(slide, M, 7.03, 9.0, 0.25,
         "Campus Lost & Found Management System  ·  OOPS + DBMS Mini Project",
         8.5, False, MUTED)
    text(slide, W - M - 0.8, 7.03, 0.8, 0.25, f"{number:02d}", 9, True, MUTED,
         align=PP_ALIGN.RIGHT)
    return content_top


def notes(slide, value):
    slide.notes_slide.notes_text_frame.text = value.strip()


def _cell_borders(cell, bottom=BORDER, width=0.75):
    """PowerPoint draws a heavy black grid on every table by default. Replace
    the cell border set with nothing, plus one hairline under each data row."""
    tcPr = cell._tc.get_or_add_tcPr()
    for tag in ("a:lnB", "a:lnT", "a:lnR", "a:lnL"):
        ln = tcPr.makeelement(qn(tag), {"w": str(int(width * 12700)),
                                        "cap": "flat", "cmpd": "sng", "algn": "ctr"})
        if tag == "a:lnB" and bottom is not None:
            fill = ln.makeelement(qn("a:solidFill"), {})
            clr = ln.makeelement(qn("a:srgbClr"), {"val": str(bottom)})
            fill.append(clr)
            ln.append(fill)
        else:
            ln.append(ln.makeelement(qn("a:noFill"), {}))
        tcPr.insert(0, ln)


def add_table(slide, x, y, w, data, ratios, row_h=0.36, size=11,
              header=True, aligns=None, header_fill=NAVY):
    rows, cols = len(data), len(data[0])
    shp = slide.shapes.add_table(rows, cols, Inches(x), Inches(y),
                                 Inches(w), Inches(row_h * rows))
    tbl = shp.table
    tblPr = tbl._tbl.tblPr
    for el in tblPr.findall(qn("a:tableStyleId")):
        tblPr.remove(el)
    tbl.first_row = False
    tbl.horz_banding = False

    for i, ratio in enumerate(ratios):
        tbl.columns[i].width = Inches(w * ratio)

    for ri, row in enumerate(data):
        tbl.rows[ri].height = Inches(row_h)
        for ci, value in enumerate(row):
            cell = tbl.cell(ri, ci)
            cell.margin_left = Inches(0.1)
            cell.margin_right = Inches(0.06)
            cell.margin_top = Inches(0.02)
            cell.margin_bottom = Inches(0.02)
            cell.vertical_anchor = MSO_ANCHOR.MIDDLE
            cell.fill.solid()
            is_head = header and ri == 0
            cell.fill.fore_color.rgb = header_fill if is_head else (
                WHITE if ri % 2 == 1 else SOFT)
            _cell_borders(cell, bottom=None if is_head else BORDER)

            tf = cell.text_frame
            tf.word_wrap = True
            p = tf.paragraphs[0]
            p.alignment = aligns[ci] if aligns else PP_ALIGN.LEFT
            p.line_spacing = 0.95
            add_rich(p, value, size, WHITE if is_head else TEXT, bold=is_head)
    return tbl


def code_block(slide, x, y, w, h, body, title=None, size=10, color=CODE):
    """Dark code panel. Text is centred vertically so the box never shows a
    suspicious empty band at the bottom."""
    box(slide, x, y, w, h, fill=INK, line=None, radius=0.04)
    if title:
        text(slide, x + 0.2, y + 0.13, w - 0.35, 0.26, title, 8.5, True, CODE_KEY,
             "Consolas")
        return text(slide, x + 0.2, y + 0.44, w - 0.36, h - 0.54, body,
                    size, False, color, "Consolas", spacing=0.98)
    return text(slide, x + 0.2, y + 0.12, w - 0.36, h - 0.24, body,
                size, False, color, "Consolas", spacing=0.98,
                anchor=MSO_ANCHOR.MIDDLE)


def pill(slide, x, y, w, h, label, fill=ACCENT_LIGHT, color=ACCENT_DARK, size=11):
    box(slide, x, y, w, h, fill=fill, line=None, radius=0.5)
    text(slide, x, y, w, h, label, size, True, color,
         align=PP_ALIGN.CENTER, anchor=MSO_ANCHOR.MIDDLE)


def screenshot_slot(slide, x, y, w, h, title, caption="Insert screenshot at 1280 × 800"):
    shp = box(slide, x, y, w, h, fill=RGBColor(0xEA, 0xF1, 0xF6), line=RGBColor(0xB9, 0xCF, 0xDE),
              radius=0.03)
    shp.line.dash_style = MSO_LINE_DASH_STYLE.DASH
    text(slide, x, y + h / 2 - 0.34, w, 0.3, "▣  SCREENSHOT", 10, True,
         ACCENT_DARK, align=PP_ALIGN.CENTER)
    text(slide, x, y + h / 2, w, 0.32, title, 13, True, NAVY, align=PP_ALIGN.CENTER)
    text(slide, x, y + h / 2 + 0.32, w, 0.26, caption, 8.5, False, MUTED,
         align=PP_ALIGN.CENTER)


def stat(slide, x, y, w, value, caption, color):
    box(slide, x, y, w, 1.55, fill=WHITE)
    slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(x), Inches(y + 0.12),
                           Inches(0.06), Inches(1.31)).fill.solid()
    bar = slide.shapes[-1]
    bar.fill.fore_color.rgb = color
    bar.line.fill.background()
    text(slide, x + 0.24, y + 0.2, w - 0.4, 0.7, value, 34, True, TEXT)
    text(slide, x + 0.24, y + 0.95, w - 0.4, 0.4, caption, 10.5, False, MUTED)


# ----------------------------------------------------------------------------
#  1. Title
# ----------------------------------------------------------------------------
def slide_01():
    s = new_slide(NAVY)

    for (cx, cy, d, alpha) in [(8.2, 4.3, 5.4, 7), (10.6, -1.9, 4.2, 5)]:
        oval = s.shapes.add_shape(MSO_SHAPE.OVAL, Inches(cx), Inches(cy), Inches(d), Inches(d))
        oval.fill.solid()
        oval.fill.fore_color.rgb = RGBColor(0x9F, 0xC9, 0xDD)
        oval.line.fill.background()
        oval.shadow.inherit = False
        solid = oval.fill._xPr.find(qn("a:solidFill"))
        clr = solid.find(qn("a:srgbClr"))
        alpha_el = clr.makeelement(qn("a:alpha"), {"val": str(alpha * 1000)})
        clr.append(alpha_el)

    text(s, M, 0.72, 3.0, 0.3, "R  E  C", 11, True, TEAL)
    text(s, M, 1.55, 9.8, 0.95, "Campus Lost & Found", 46, True, WHITE)
    text(s, M, 2.48, 9.8, 0.6, "Management System", 27, False, RGBColor(0xC9, 0xD6, 0xE2))

    s.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(M + 0.03), Inches(3.24),
                       Inches(1.55), Inches(0.07)).fill.solid()
    rule = s.shapes[-1]
    rule.fill.fore_color.rgb = ACCENT
    rule.line.fill.background()

    text(s, M, 3.5, 8.9, 0.9,
         "One searchable desk for every lost bottle, ID card, key and\ncalculator on campus.",
         15.5, False, RGBColor(0xC9, 0xD6, 0xE2), spacing=1.3)

    line = s.shapes.add_connector(MSO_CONNECTOR.STRAIGHT, Inches(M), Inches(5.42),
                                  Inches(W - M), Inches(5.42))
    line.line.color.rgb = RGBColor(0x35, 0x55, 0x70)
    line.line.width = Pt(1)

    columns = [
        ("PRESENTED BY", "‹Your Name›  ·  ‹Roll Number›"),
        ("GUIDE", "‹Guide Name›  ·  ‹Department›"),
        ("ACADEMIC YEAR", "2025 – 26"),
    ]
    for i, (label, value) in enumerate(columns):
        x = M + i * 4.1
        text(s, x, 5.62, 3.9, 0.25, label, 8.5, True, RGBColor(0x6E, 0x8C, 0xA8))
        text(s, x, 5.9, 3.9, 0.3, value, 12.5, False, WHITE)

    text(s, W - M - 3.2, 7.02, 3.2, 0.25, "Java  ·  Swing  ·  JDBC  ·  MySQL",
         9, True, TEAL, align=PP_ALIGN.RIGHT)

    notes(s, """
Good morning. Our project is a Campus Lost and Found Management System. It replaces
notice boards and WhatsApp forwards with one searchable database, and it is built to
demonstrate OOPS and DBMS concepts in a real application.
""")


# ----------------------------------------------------------------------------
#  2. Agenda
# ----------------------------------------------------------------------------
def slide_02():
    s = new_slide()
    chrome(s, "Agenda", "From the problem, to the design, to a live demonstration", 2)

    items = [
        "Problem and motivation", "Objectives and scope",
        "Existing systems and gaps", "Technology stack",
        "System architecture", "OOPS design",
        "Database design", "Transactions and matching",
        "Module walkthrough", "Testing, limits, conclusion",
    ]
    for i, item in enumerate(items):
        col = i // 5
        row = i % 5
        x = M + 0.15 + col * 6.05
        y = 1.95 + row * 0.92
        box(s, x, y, 0.46, 0.46, fill=ACCENT_LIGHT, line=None, radius=0.5)
        text(s, x, y, 0.46, 0.46, f"{i + 1:02d}", 10, True, ACCENT_DARK,
             align=PP_ALIGN.CENTER, anchor=MSO_ANCHOR.MIDDLE)
        text(s, x + 0.68, y + 0.02, 4.9, 0.42, item, 14.5, False, TEXT,
             anchor=MSO_ANCHOR.MIDDLE)

    text(s, M + 0.15, 6.55, CW - 0.3, 0.3,
         "Plus a 5-minute live demo: report → match → claim → verify → recover.",
         10.5, True, MUTED)

    notes(s, """
We will move from why the project exists, to how it is designed, to a live
demonstration, and finish with testing and future scope.
""")


# ----------------------------------------------------------------------------
#  3. Problem
# ----------------------------------------------------------------------------
def slide_03():
    s = new_slide()
    chrome(s, "The problem", "Information exists, but it is scattered and unverifiable", 3)

    cards = [
        ("1", "Fragmented reporting", DANGER,
         "Notice boards, WhatsApp groups and a paper register. Each channel reaches a different audience."),
        ("2", "No proof of ownership", WARNING,
         "Anyone can walk to the security office and describe an item well enough to take it."),
        ("3", "No audit trail", ACCENT,
         "The college cannot say how many items were lost, claimed or actually recovered."),
    ]
    for i, (num, title, color, body) in enumerate(cards):
        x = M + i * 4.09
        box(s, x, 1.82, 3.7, 2.35, fill=WHITE)
        circle = s.shapes.add_shape(MSO_SHAPE.OVAL, Inches(x + 0.28), Inches(2.05),
                                    Inches(0.52), Inches(0.52))
        circle.fill.solid()
        circle.fill.fore_color.rgb = color
        circle.line.fill.background()
        circle.shadow.inherit = False
        text(s, x + 0.28, 2.05, 0.52, 0.52, num, 14, True, WHITE,
             align=PP_ALIGN.CENTER, anchor=MSO_ANCHOR.MIDDLE)
        text(s, x + 0.28, 2.72, 3.15, 0.35, title, 14.5, True, TEXT)
        text(s, x + 0.28, 3.12, 3.12, 0.9, body, 11.5, False, MUTED, spacing=1.15)

    strip = box(s, M, 4.55, CW, 0.78, fill=RGBColor(0xFD, 0xEC, 0xEA), line=None, radius=0.08)
    s.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(M), Inches(4.68),
                       Inches(0.06), Inches(0.52)).fill.solid()
    bar = s.shapes[-1]
    bar.fill.fore_color.rgb = DANGER
    bar.line.fill.background()
    text(s, M + 0.3, 4.55, CW - 0.5, 0.78,
         "Dozens of items are lost every month — only a handful are ever returned.",
         15, True, RGBColor(0x8E, 0x2B, 0x20), anchor=MSO_ANCHOR.MIDDLE)

    text(s, M, 5.65, CW, 0.9,
         "Ask the room: how many of you lost a bottle, ID card or charger this semester?\n"
         "The information exists — it simply never meets the person who needs it.",
         12, False, MUTED, spacing=1.4)

    notes(s, """
Ask the room: how many of you have lost a bottle, ID card or charger this semester?
The information exists, but it is scattered. The security office keeps a register,
students post in groups, and the two never meet. That gap is our problem statement.
""")


# ----------------------------------------------------------------------------
#  4. Solution
# ----------------------------------------------------------------------------
def slide_04():
    s = new_slide()
    chrome(s, "Proposed solution", "One platform, four steps", 4)

    steps = [
        ("01", "Report", "Lost or found item with description, category, location and date.", ACCENT),
        ("02", "Match", "Automatic candidate pairs scored out of 100 by category, place and date.", ACCENT_DARK),
        ("03", "Claim", "Owner submits proof of ownership; staff verify it before handover.", WARNING),
        ("04", "Recover", "Item handed over, status updated, audit row written by a trigger.", SUCCESS),
    ]
    total = 4 * 2.62 + 3 * 0.38
    start = (W - total) / 2
    for i, (num, title, body, color) in enumerate(steps):
        x = start + i * 3.0
        box(s, x, 1.95, 2.62, 2.55, fill=WHITE)
        text(s, x + 0.24, 2.15, 1.0, 0.3, num, 11, True, color, "Consolas")
        text(s, x + 0.24, 2.48, 2.2, 0.4, title, 17, True, NAVY)
        text(s, x + 0.24, 2.95, 2.15, 1.3, body, 10.5, False, MUTED, spacing=1.2)
        if i < 3:
            arrow = s.shapes.add_shape(MSO_SHAPE.RIGHT_ARROW, Inches(x + 2.72),
                                       Inches(3.1), Inches(0.18), Inches(0.24))
            arrow.fill.solid()
            arrow.fill.fore_color.rgb = RGBColor(0xB9, 0xCF, 0xDE)
            arrow.line.fill.background()
            arrow.shadow.inherit = False

    box(s, M, 4.85, CW, 0.95, fill=ACCENT_LIGHT, line=None, radius=0.08)
    text(s, M + 0.32, 4.85, CW - 0.6, 0.95,
         "Every report feeds the matching pool  ·  every claim has a verifier  ·  every "
         "handover is recorded",
         13.5, True, ACCENT_DARK, anchor=MSO_ANCHOR.MIDDLE)

    text(s, M, 6.0, CW, 0.5,
         "The result: one loop that turns scattered posts into a measurable recovery process.",
         12, False, MUTED)

    notes(s, """
The whole system is one loop. Every report improves the matching pool, every claim
has a verifier, and every recovery is recorded, so the college finally gets numbers.
""")


# ----------------------------------------------------------------------------
#  5. Objectives
# ----------------------------------------------------------------------------
def slide_05():
    s = new_slide()
    chrome(s, "Objectives", "What the system must achieve", 5)

    objectives = [
        ("Single repository", "One searchable record of every lost and found item on campus."),
        ("Faster recovery", "Suggest matches automatically instead of relying on someone scrolling a group."),
        ("Verified ownership", "No item is released without checked proof and a recorded verifier."),
        ("Management reporting", "Live counts of lost, found, claimed and recovered items, exportable to CSV."),
        ("Academic demonstration", "Use all four OOPS pillars and the core DBMS features for a real reason."),
    ]
    for i, (title, body) in enumerate(objectives):
        y = 1.85 + i * 0.95
        text(s, M + 0.1, y, 0.55, 0.35, f"{i + 1}", 16, True, ACCENT, "Consolas")
        text(s, M + 0.75, y - 0.03, 3.1, 0.35, title, 14.5, True, NAVY)
        text(s, M + 4.0, y - 0.02, 7.7, 0.6, body, 11.5, False, MUTED, spacing=1.1)
        if i < 4:
            line = s.shapes.add_connector(MSO_CONNECTOR.STRAIGHT, Inches(M + 0.1),
                                          Inches(y + 0.72), Inches(W - M), Inches(y + 0.72))
            line.line.color.rgb = RGBColor(0xE6, 0xEC, 0xF2)
            line.line.width = Pt(1)

    text(s, M, 6.5, CW, 0.3,
         "Points 1–4 are user-facing. Point 5 is academic: the concepts are used because the "
         "problem needs them.",
         10.5, False, MUTED, italic=True)

    notes(s, """
Points one to four are the user-facing objectives. Point five is academic: this is not
a CRUD toy, every concept in the syllabus is used because the problem genuinely needs it.
""")


# ----------------------------------------------------------------------------
#  6. Scope
# ----------------------------------------------------------------------------
def slide_06():
    s = new_slide()
    chrome(s, "Scope and stakeholders", "A deliberately tight boundary, so the project could be finished and tested", 6)

    box(s, M, 1.8, 5.85, 3.15, fill=WHITE)
    text(s, M + 0.3, 1.98, 4.0, 0.3, "IN SCOPE", 9.5, True, SUCCESS, "Consolas")
    bullets(s, M + 0.3, 2.38, 5.25, [
        "Single campus, single MySQL database",
        "Desktop client for students, staff and admin",
        "Lost and found reports with search and filters",
        "Automatic matching and verified claims",
        "Recovery tracking, audit trail and CSV reports",
    ], size=11.5, gap=7, bullet_color=SUCCESS)

    box(s, M + 6.05, 1.8, 5.85, 3.15, fill=WHITE)
    text(s, M + 6.35, 1.98, 4.0, 0.3, "OUT OF SCOPE  (FUTURE)", 9.5, True, MUTED, "Consolas")
    bullets(s, M + 6.35, 2.38, 5.25, [
        "Mobile app and e-mail / SMS notifications",
        "Photo upload and image similarity",
        "QR-code tags on lab equipment",
        "Cloud deployment and multi-campus tenancy",
        "Single sign-on with the college portal",
    ], size=11.5, gap=7, bullet_color=MUTED)

    text(s, M, 5.28, 3.0, 0.25, "ACTORS", 9.5, True, MUTED, "Consolas")
    pill(s, M, 5.6, 1.85, 0.46, "Student")
    pill(s, M + 2.05, 5.6, 2.15, 0.46, "Security staff", ACCENT, WHITE)
    pill(s, M + 4.4, 5.6, 1.95, 0.46, "Administrator", NAVY, WHITE)

    text(s, M, 6.35, CW, 0.4,
         "Each out-of-scope item is a natural next step, not a rewrite — the layers are separated.",
         11.5, False, MUTED, italic=True)

    notes(s, """
We deliberately kept the boundary tight so the project could be finished and tested
properly. Each out-of-scope item is a natural next step, which I will come back to at
the end.
""")


# ----------------------------------------------------------------------------
#  7. Comparison
# ----------------------------------------------------------------------------
def slide_07():
    s = new_slide()
    chrome(s, "Existing systems and gaps", "The paper register is the closest alternative — and it fails on three counts", 7)

    data = [
        ["Channel", "Searchable", "Ownership proof", "Audit trail"],
        ["Notice board", "No", "No", "No"],
        ["WhatsApp group", "Partly (scrolling)", "No", "No"],
        ["Paper register", "No", "Informal", "Manual"],
        ["This system", "Yes — SQL", "Yes — claims", "Yes — triggers"],
    ]
    add_table(s, M + 0.1, 1.9, CW - 0.2, data, [0.34, 0.22, 0.22, 0.22],
              row_h=0.55, size=12.5, aligns=[PP_ALIGN.LEFT] * 4)

    box(s, M + 0.1, 5.05, CW - 0.2, 1.35, fill=WHITE)
    text(s, M + 0.4, 5.2, 3.0, 0.3, "THE GAP", 9.5, True, ACCENT, "Consolas")
    text(s, M + 0.4, 5.52, CW - 0.9, 0.8,
         "A record that cannot be searched is not a system. A handover that cannot be "
         "verified is not a process. A semester that cannot be summarised is not management.",
         12, False, TEXT, spacing=1.3)

    notes(s, """
The paper register is the closest existing system, and it fails on three counts: you
cannot search it, anyone can claim an item, and nobody can produce a report at the end
of the semester.
""")


# ----------------------------------------------------------------------------
#  8. Technology stack
# ----------------------------------------------------------------------------
def slide_08():
    s = new_slide()
    chrome(s, "Technology stack", "No framework, on purpose: SQL and OOPS stay visible", 8)

    data = [
        ["Layer", "Technology", "Responsibility"],
        ["Presentation", "Java Swing / AWT", "Windows, forms, tables, dialogs"],
        ["Business", "Plain Java classes", "Validation, rules, transactions"],
        ["Persistence", "JDBC + DAO pattern", "SQL execution, row mapping"],
        ["Database", "MySQL 8", "Tables, keys, views, triggers, transactions"],
    ]
    add_table(s, M + 0.1, 1.85, CW - 0.2, data, [0.20, 0.27, 0.53],
              row_h=0.52, size=12, aligns=[PP_ALIGN.LEFT] * 3)

    box(s, M + 0.1, 4.85, CW - 0.2, 1.6, fill=WHITE)
    text(s, M + 0.35, 5.0, 2.6, 0.3, "WHY THIS STACK", 9.5, True, ACCENT, "Consolas")
    text(s, M + 0.35, 5.32, CW - 0.9, 1.0,
         "Spring or Hibernate would hide exactly what the review wants to see: joins, "
         "prepared statements, transactions and object-relational mapping. Every layer here "
         "is small enough to read in the viva.",
         11.5, False, TEXT, spacing=1.3)

    notes(s, """
We chose no framework on purpose. Spring or Hibernate would hide exactly the concepts
the review wants to see: joins, transactions, prepared statements and object-relational
mapping.
""")


# ----------------------------------------------------------------------------
#  9. Architecture
# ----------------------------------------------------------------------------
def slide_09():
    s = new_slide()
    chrome(s, "System architecture", "Three layers, one dependency direction: downwards only", 9)

    layers = [
        ("PRESENTATION", "Swing", "LoginFrame · MainFrame · 6 panels · dialogs", NAVY),
        ("SERVICE", "Business rules", "Auth · Item · Claim · Match · Report", NAVY_SOFT),
        ("PERSISTENCE", "JDBC / DAO", "GenericDAO · 4 JDBC DAOs · row mapping", ACCENT_DARK),
        ("DATABASE", "MySQL 8", "8 tables · 2 views · 2 triggers · 6 indexes", ACCENT),
    ]
    for i, (name, tag, detail, color) in enumerate(layers):
        y = 1.82 + i * 1.2
        box(s, 0.95, y, 5.9, 0.92, fill=color, line=None, radius=0.06)
        text(s, 1.15, y + 0.13, 2.1, 0.25, name, 10.5, True, WHITE, "Consolas")
        text(s, 3.05, y + 0.11, 2.0, 0.25, tag, 9, True, RGBColor(0x9F, 0xC9, 0xDD),
             align=PP_ALIGN.RIGHT)
        text(s, 1.15, y + 0.44, 5.4, 0.35, detail, 9.5, False, RGBColor(0xE3, 0xEE, 0xF5))
        if i < 3:
            arrow = s.shapes.add_shape(MSO_SHAPE.DOWN_ARROW, Inches(3.72), Inches(y + 0.95),
                                       Inches(0.32), Inches(0.22))
            arrow.fill.solid()
            arrow.fill.fore_color.rgb = RGBColor(0xB9, 0xCF, 0xDE)
            arrow.line.fill.background()
            arrow.shadow.inherit = False

    box(s, 7.25, 1.82, 5.35, 4.30, fill=WHITE)
    text(s, 7.5, 2.0, 4.0, 0.3, "LAYER RULES", 9.5, True, ACCENT, "Consolas")
    bullets(s, 7.5, 2.42, 4.85, [
        "UI contains **no SQL** and no JDBC import.",
        "Service contains **no Swing** import.",
        "DAO holds no business rules — only statements and mapping.",
        "Services depend on **interfaces**, not on JdbcXxxDAO.",
        "One transaction boundary: `ClaimService`.",
    ], size=11, gap=12)

    text(s, 7.5, 5.26, 4.85, 0.75,
         "Swap MySQL for another database by writing new DAO classes only.",
         10.5, False, MUTED, italic=True, spacing=1.3)

    notes(s, """
Each layer only knows the one below it. The UI has no SQL at all; the DAO has no
business rules; the service has no Swing imports. That is what makes the claim
transaction reusable and the code testable.
""")


# ----------------------------------------------------------------------------
#  10. OOPS at a glance
# ----------------------------------------------------------------------------
def slide_10():
    s = new_slide()
    chrome(s, "OOPS at a glance", "Four pillars, one hierarchy — seen from four angles", 10)

    quadrants = [
        ("Encapsulation", "Data is private; behaviour is public.", "Item · User · DBConnection",
         "`setStatus` never allows null", ACCENT),
        ("Abstraction", "Expose what, hide how.", "abstract Item · GenericDAO<T>",
         "UI asks for an item type, not a class", ACCENT_DARK),
        ("Inheritance", "Reuse common state and behaviour.", "LostItem / FoundItem extend Item",
         "Common fields declared once", WARNING),
        ("Polymorphism", "One reference, many runtime forms.", "List<Item> · overridden methods",
         "No instanceof in the UI", SUCCESS),
    ]
    for i, (title, line, where, proof, color) in enumerate(quadrants):
        x = M + (i % 2) * 6.06
        y = 1.8 + (i // 2) * 2.25
        box(s, x, y, 5.83, 2.03, fill=WHITE)
        s.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(x + 0.28), Inches(y + 0.3),
                           Inches(0.06), Inches(0.42)).fill.solid()
        bar = s.shapes[-1]
        bar.fill.fore_color.rgb = color
        bar.line.fill.background()
        text(s, x + 0.48, y + 0.24, 4.9, 0.35, title, 15, True, NAVY)
        text(s, x + 0.48, y + 0.68, 5.05, 0.3, line, 11.5, False, TEXT)
        text(s, x + 0.48, y + 1.05, 5.05, 0.3, where, 10, True, color, "Consolas")
        text(s, x + 0.48, y + 1.42, 5.05, 0.35, proof, 9.5, False, MUTED, italic=True)

    notes(s, """
These are not four separate examples bolted on; they are the same hierarchy seen from
four angles.
""")


# ----------------------------------------------------------------------------
#  11. Class hierarchy
# ----------------------------------------------------------------------------
def slide_11():
    s = new_slide()
    chrome(s, "Class design: the Item hierarchy", "The single best example of inheritance, abstraction and polymorphism", 11)

    # abstract super-class
    box(s, 3.9, 1.78, 5.5, 1.75, fill=WHITE, line=NAVY, radius=0.03)
    text(s, 4.1, 1.88, 5.2, 0.25, "«abstract»  Item", 10, True, ACCENT_DARK, "Consolas")
    text(s, 4.1, 2.14, 5.2, 0.25, "Item", 15, True, NAVY)
    text(s, 4.1, 2.45, 5.1, 0.4,
         "- itemId · title · description · category · location\n- eventDate · reportedBy · status",
         8.8, False, MUTED, "Consolas", spacing=1.1)
    text(s, 4.1, 3.02, 5.1, 0.4,
         "+ getItemType()        «abstract»\n+ getSpecificDetails()  «abstract»",
         8.8, False, TEXT, "Consolas", spacing=1.1)

    # sub-classes
    box(s, 1.35, 4.3, 4.35, 1.85, fill=WHITE, line=BORDER, radius=0.03)
    text(s, 1.55, 4.42, 3.9, 0.28, "LostItem", 14, True, WARNING)
    text(s, 1.55, 4.72, 3.9, 0.9,
         "- contact : String\n- rewardOffered : BigDecimal\n- urgency : Urgency",
         9, False, MUTED, "Consolas", spacing=1.15)
    text(s, 1.55, 5.5, 3.9, 0.55,
         "+ getItemType() → LOST\n+ getSpecificDetails()", 9, False, TEXT, "Consolas", spacing=1.15)

    box(s, 7.65, 4.3, 4.35, 1.85, fill=WHITE, line=BORDER, radius=0.03)
    text(s, 7.85, 4.42, 3.9, 0.28, "FoundItem", 14, True, ACCENT_DARK)
    text(s, 7.85, 4.72, 3.9, 0.9,
         "- storageLocation : String\n- handedToSecurity : boolean\n- finderNotes : String",
         9, False, MUTED, "Consolas", spacing=1.15)
    text(s, 7.85, 5.5, 3.9, 0.55,
         "+ getItemType() → FOUND\n+ getSpecificDetails()", 9, False, TEXT, "Consolas", spacing=1.15)

    # inheritance connectors
    for (x1, y1, x2, y2) in [(5.55, 3.53, 3.53, 4.3), (7.75, 3.53, 9.83, 4.3)]:
        line = s.shapes.add_connector(MSO_CONNECTOR.STRAIGHT, Inches(x1), Inches(y1),
                                      Inches(x2), Inches(y2))
        line.line.color.rgb = NAVY
        line.line.width = Pt(1.4)

    # mapping chips
    chips = ["Common state lives once", "Sub-classes add only specifics", "Mirrored by class-table inheritance"]
    xs = [0.79, 4.88, 8.97]
    for x, label in zip(xs, chips):
        pill(s, x, 6.35, 3.55, 0.38, label, fill=ACCENT_LIGHT, color=ACCENT_DARK, size=9.5)

    notes(s, """
This is the core of the OOPS design. If you asked for a single example of inheritance,
abstraction and polymorphism, this hierarchy is it.
""")


# ----------------------------------------------------------------------------
#  12. Encapsulation / abstraction code
# ----------------------------------------------------------------------------
def slide_12():
    s = new_slide()
    chrome(s, "Encapsulation and abstraction", "Not just getters and setters — the object protects its invariants", 12)

    text(s, M, 1.72, 5.6, 0.3, "ENCAPSULATION  ·  Item.java", 9.5, True, ACCENT, "Consolas")
    code_block(s, M, 2.06, 5.8, 3.05, """private String title;                // no direct access
private ItemStatus status = ItemStatus.OPEN;

public void setStatus(ItemStatus status) {
    // invariant: status is never null
    this.status = status == null
            ? ItemStatus.OPEN : status;
}

public String getTitle() { return title; }""", size=10)

    text(s, M + 6.05, 1.72, 5.6, 0.3, "ABSTRACTION  ·  Item.java + GenericDAO.java", 9.5, True, ACCENT_DARK, "Consolas")
    code_block(s, M + 6.05, 2.06, 5.83, 3.05, """public abstract class Item {
    public abstract ItemType getItemType();
    public abstract String getSpecificDetails();
}

public interface GenericDAO<T> {
    T findById(int id);
    List<T> findAll();
    int insert(T entity);
    boolean update(T entity);
}""", size=10)

    box(s, M, 5.38, CW, 1.05, fill=WHITE)
    text(s, M + 0.3, 5.52, CW - 0.6, 0.8,
         "The UI never touches a field directly, and it never learns that JDBC exists. "
         "Both pillars are enforced by the compiler, not by convention.",
         11.5, False, TEXT, spacing=1.3, anchor=MSO_ANCHOR.MIDDLE)

    notes(s, """
Encapsulation is not just getters and setters: the setter enforces the invariant that
status is never null. Abstraction means the UI asks "are you lost or found?" without
knowing which class it holds, and the service asks the DAO for data without knowing
that JDBC exists.
""")


# ----------------------------------------------------------------------------
#  13. Inheritance / polymorphism code
# ----------------------------------------------------------------------------
def slide_13():
    s = new_slide()
    chrome(s, "Inheritance and polymorphism", "One loop, no type checks, two behaviours", 13)

    text(s, M, 1.7, 6.6, 0.3, "INHERITANCE + DYNAMIC DISPATCH", 9.5, True, ACCENT, "Consolas")
    code_block(s, M, 2.02, 6.75, 3.3, """public class LostItem extends Item {
    @Override public ItemType getItemType() {
        return ItemType.LOST;
    }
    @Override public String getSpecificDetails() {
        return "Contact: " + contact
             + "\\nReward: Rs " + rewardOffered;
    }
}

// One reference type, two behaviours:
List<Item> items = itemDAO.findAll();
for (Item item : items) {
    row.add(item.getItemType().getLabel());
    row.add(item.getSpecificDetails());   // runtime
}""", size=9.5)

    text(s, M + 7.05, 1.7, 4.8, 0.3, "SAME CALL, DIFFERENT OUTPUT", 9.5, True, ACCENT_DARK, "Consolas")
    data = [
        ["#", "Runtime object", "getSpecificDetails()"],
        ["1", "LostItem", "Contact / Reward / Urgency"],
        ["2", "FoundItem", "Storage / Security / Notes"],
    ]
    add_table(s, M + 7.05, 2.02, 4.85, data, [0.12, 0.3, 0.58],
              row_h=0.58, size=9.5, aligns=[PP_ALIGN.LEFT] * 3)

    box(s, M + 7.05, 4.15, 4.85, 1.15, fill=ACCENT_LIGHT, line=None, radius=0.06)
    text(s, M + 7.25, 4.28, 4.45, 0.9,
         "A new item type needs one new sub-class — this loop is never edited.",
         10.5, True, ACCENT_DARK, spacing=1.25)

    text(s, M, 5.55, 6.75, 0.5,
         "instanceof appears only in the DAO, where a table must be chosen.",
         10, False, MUTED, italic=True)

    notes(s, """
The loop has no if-else on type. Java dispatches to the right override, which is
exactly what polymorphism buys us: new item types can be added without editing this
loop.
""")


# ----------------------------------------------------------------------------
#  14. ER diagram
# ----------------------------------------------------------------------------
def slide_14():
    s = new_slide()
    chrome(s, "Database design: ER diagram", "8 tables, 9 relationships, class-table inheritance in the middle", 14)

    def entity(x, y, w, h, name, fields, fill=WHITE, border=BORDER, title_color=NAVY):
        box(s, x, y, w, h, fill=fill, line=border, radius=0.03)
        text(s, x + 0.1, y + 0.08, w - 0.2, 0.22, name, 8.5, True, title_color, "Consolas")
        text(s, x + 0.1, y + 0.34, w - 0.2, h - 0.4, fields, 7.2, False, MUTED,
             "Consolas", spacing=1.05)

    entity(0.80, 1.68, 2.30, 1.00, "CATEGORIES", "category_id  PK\nname         UQ\ndescription")
    entity(3.35, 1.68, 2.35, 1.00, "LOCATIONS", "location_id  PK\nname         UQ\nbuilding")
    entity(9.65, 1.68, 2.95, 1.00, "USERS", "user_id  PK\nemail    UQ\nphone · role")

    entity(3.35, 3.15, 3.55, 1.68, "ITEMS", "item_id      PK\nitem_type    LOST|FOUND\ntitle · description\nevent_date · status\ncategory · location\nreporter",
           fill=NAVY, border=NAVY, title_color=WHITE)

    entity(0.80, 3.50, 2.25, 1.18, "LOST_ITEMS", "item_id  PK FK\ncontact\nreward · urgency", fill=WHITE)
    entity(7.20, 3.50, 2.10, 1.18, "FOUND_ITEMS", "item_id  PK FK\nstorage_location\nhanded · notes", fill=WHITE)
    entity(3.35, 5.12, 3.55, 1.32, "ITEM_STATUS_HISTORY", "history_id  PK\nitem_id     FK\nold · new · changed_at")
    entity(8.30, 5.12, 4.30, 1.32, "CLAIMS", "claim_id     PK\nitem_id      FK\nclaimant     FK\nverifier     FK\nstatus · proof")

    def link(x1, y1, x2, y2):
        line = s.shapes.add_connector(MSO_CONNECTOR.STRAIGHT, Inches(x1), Inches(y1),
                                      Inches(x2), Inches(y2))
        line.line.color.rgb = RGBColor(0x9F, 0xB6, 0xC8)
        line.line.width = Pt(1.2)

    # reference tables down into items
    link(1.95, 2.68, 3.60, 3.15)          # categories → items
    link(4.52, 2.68, 4.52, 3.15)          # locations  → items
    link(9.65, 2.20, 6.90, 3.40)          # users      → items (reporter)
    # items out to its two subtypes
    link(3.05, 4.09, 3.35, 4.09)          # lost_items → items
    link(6.90, 4.09, 7.20, 4.09)          # items      → found_items
    # items down to the audit table
    link(5.12, 4.83, 5.12, 5.12)
    # users down to claims
    link(11.12, 2.68, 11.12, 5.12)
    # items → claims, routed around the audit table
    link(7.02, 4.83, 7.02, 5.02)
    link(7.02, 5.02, 8.30, 5.02)

    # cardinality hints
    for x, y, label in [(2.42, 2.76, "1:N"), (4.60, 2.76, "1:N"), (8.05, 2.58, "1:N"),
                        (3.10, 3.74, "1:1"), (6.94, 3.74, "1:1"),
                        (5.20, 4.86, "1:N"), (11.20, 3.66, "1:N"), (7.08, 4.84, "1:N")]:
        text(s, x, y, 0.5, 0.18, label, 6.5, True, ACCENT_DARK, "Consolas")

    notes(s, """
The interesting part is the middle: items is the super-table, and the two subtype
tables hold only the fields specific to lost or found reports. That is the database
mirror of the Java hierarchy.
""")


# ----------------------------------------------------------------------------
#  15. Schema and constraints
# ----------------------------------------------------------------------------
def slide_15():
    s = new_slide()
    chrome(s, "Schema, keys and constraints", "Referential integrity is enforced by the database, not just by Java", 15)

    data = [
        ["Table", "PK", "Foreign keys"],
        ["users", "user_id", "—"],
        ["categories", "category_id", "—"],
        ["locations", "location_id", "—"],
        ["items", "item_id", "category · location · reporter"],
        ["lost_items", "item_id", "→ items (1:1, cascade)"],
        ["found_items", "item_id", "→ items (1:1, cascade)"],
        ["claims", "claim_id", "item · claimant · verifier"],
        ["item_status_history", "history_id", "→ items (cascade)"],
    ]
    add_table(s, M, 1.82, 7.55, data, [0.34, 0.22, 0.44], row_h=0.42, size=9.5)

    box(s, M + 7.95, 1.82, 3.95, 3.95, fill=WHITE)
    text(s, M + 8.18, 1.98, 3.5, 0.3, "CONSTRAINTS USED", 9, True, ACCENT, "Consolas")
    bullets(s, M + 8.18, 2.36, 3.5, [
        "**NOT NULL** — required columns",
        "**UNIQUE** — e-mail, category, location",
        "**UNIQUE (item_id, claimant_id)** — no duplicate claim",
        "**CHECK** — reward ≥ 0, phone ≥ 10 digits",
        "**ENUM** — status domains",
        "**ON DELETE CASCADE** — subtype rows",
        "**6 indexes** — search and join columns",
    ], size=9.2, gap=6)

    text(s, M, 6.18, 7.55, 0.5,
         "Two constraints matter most: the unique claim key and the 1:1 subtype keys.",
         10.5, False, MUTED, italic=True)

    notes(s, """
Two constraints deserve a mention. The unique key on item + claimant blocks duplicate
claims at database level, not just in Java. And the foreign keys make it impossible to
attach a claim to a non-existent item.
""")


# ----------------------------------------------------------------------------
#  16. SQL features
# ----------------------------------------------------------------------------
def slide_16():
    s = new_slide()
    chrome(s, "SQL features in the project", "Joins, views, triggers and aggregation — each doing a real job", 16)

    text(s, M, 1.62, 5.5, 0.28, "JOIN  ·  every list screen", 9, True, ACCENT, "Consolas")
    code_block(s, M, 1.90, 5.8, 1.50, """FROM items i
JOIN categories c ON c.category_id = i.category_id
JOIN locations  l ON l.location_id = i.location_id
JOIN users      u ON u.user_id     = i.reporter_id
LEFT JOIN lost_items  li ON li.item_id = i.item_id
LEFT JOIN found_items fi ON fi.item_id = i.item_id""", size=8.3)

    text(s, M + 6.05, 1.62, 5.5, 0.28, "VIEW  ·  matching engine", 9, True, ACCENT_DARK, "Consolas")
    code_block(s, M + 6.05, 1.90, 5.83, 1.50, """CREATE OR REPLACE VIEW vw_potential_matches AS
SELECT lo.item_id AS lost_item_id, fo.item_id AS found_item_id,
       (40 + 30 + CASE ... END + CASE ... END) AS match_score
FROM items lo JOIN items fo ON ...   -- self join
HAVING match_score >= 50;""", size=8.3)

    text(s, M, 3.58, 5.5, 0.28, "TRIGGER  ·  audit without Java code", 9, True, SUCCESS, "Consolas")
    code_block(s, M, 3.86, 5.8, 1.50, """CREATE TRIGGER trg_items_after_update
AFTER UPDATE ON items FOR EACH ROW
BEGIN
  IF OLD.status <> NEW.status THEN
    INSERT INTO item_status_history ...
  END IF;
END""", size=8.3)

    text(s, M + 6.05, 3.58, 5.5, 0.28, "GROUP BY  ·  dashboard counters", 9, True, WARNING, "Consolas")
    code_block(s, M + 6.05, 3.86, 5.83, 1.50, """SELECT status, COUNT(*) AS total
FROM items
GROUP BY status;
-- 7 lost, 7 found, 2 pending claims,
-- 1 recovered in the seed data""", size=8.3)

    box(s, M, 5.60, CW, 0.95, fill=WHITE)
    text(s, M + 0.3, 5.72, CW - 0.6, 0.72,
         "The LEFT JOIN keeps a lost report even when it has no found row — an inner join would "
         "silently drop half the data. The view recomputes on every query, so no match is ever "
         "stale, and the trigger makes the audit row impossible to forget.",
         10.5, False, TEXT, spacing=1.3)

    notes(s, """
The LEFT JOIN is important: a lost row has no found_items row, so an inner join would
silently drop half the data. The trigger means the audit trail cannot be forgotten by
application code.
""")


# ----------------------------------------------------------------------------
#  17. Transactions
# ----------------------------------------------------------------------------
def slide_17():
    s = new_slide()
    chrome(s, "Transactions: the claim workflow", "One atomic unit of work — the ACID showcase", 17)

    code_block(s, M, 1.78, 6.75, 2.45, """connection.setAutoCommit(false);

// 1. the claim becomes APPROVED
UPDATE claims SET status='APPROVED', verified_by=?,
       verified_at=NOW() WHERE claim_id=?;

// 2. the item is reserved
UPDATE items SET status='CLAIMED' WHERE item_id=?;

// 3. every competing PENDING claim is closed
UPDATE claims SET status='REJECTED'
 WHERE item_id=? AND claim_id<>? AND status='PENDING';

connection.commit();     // or rollback() on any failure""", size=8.8)

    data = [
        ["Row", "Before", "After"],
        ["Claim 3", "PENDING", "APPROVED"],
        ["Claim 2 (same item)", "PENDING", "REJECTED"],
        ["Item 14", "OPEN", "CLAIMED"],
    ]
    add_table(s, M + 7.05, 1.86, 4.85, data, [0.42, 0.29, 0.29],
              row_h=0.50, size=9.5, aligns=[PP_ALIGN.LEFT, PP_ALIGN.CENTER, PP_ALIGN.CENTER])

    box(s, M + 7.05, 4.05, 4.85, 1.72, fill=WHITE)
    text(s, M + 7.25, 4.18, 4.4, 0.28, "WHY A TRANSACTION?", 9, True, DANGER, "Consolas")
    text(s, M + 7.25, 4.50, 4.45, 1.15,
         "Two staff members may approve two claims on the same item at the same moment. "
         "Without atomicity the item could be reserved twice, or an approved claim could "
         "point at an open item. The commit is the only instant at which the three rows "
         "become visible together.",
         9.5, False, TEXT, spacing=1.25)

    box(s, M, 4.55, 6.75, 1.22, fill=WHITE)
    text(s, M + 0.26, 4.66, 6.2, 0.26,
         "THE BOUNDARY  ·  ClaimService.runInTransaction()", 8.5, True, ACCENT, "Consolas")
    text(s, M + 0.26, 4.96, 6.25, 0.72,
         "auto-commit off → three statements → one commit. Any exception calls rollback(), "
         "and all three rows revert together. Reporting an item uses the same pattern for "
         "its two inserts.",
         10, False, TEXT, spacing=1.25)

    box(s, M, 5.95, CW, 0.85, fill=ACCENT_LIGHT, line=None, radius=0.06)
    text(s, M + 0.3, 5.95, CW - 0.6, 0.85,
         "A → Atomicity  ·  C → Consistency  ·  I → Isolation  ·  D → Durability",
         11, True, ACCENT_DARK, anchor=MSO_ANCHOR.MIDDLE)

    notes(s, """
This is the ACID slide. In ClaimService.runInTransaction we disable auto-commit, run the
three statements, commit once. Any exception rolls all three back, so the database can
never show an approved claim against an open item.
""")


# ----------------------------------------------------------------------------
#  18. Matching engine
# ----------------------------------------------------------------------------
def slide_18():
    s = new_slide()
    chrome(s, "Matching engine", "Candidate filter in SQL, weighted score out of 100", 18)

    data = [
        ["Signal", "Points"],
        ["Same category", "40"],
        ["Same location", "30"],
        ["Reports ≤ 2 days apart", "20"],
        ["Reports ≤ 7 days apart", "12"],
        ["Other date gap (≤ 30 days)", "5"],
        ["First title word appears in the other", "10"],
    ]
    add_table(s, M, 1.8, 5.55, data, [0.82, 0.18], row_h=0.42, size=10.5,
              aligns=[PP_ALIGN.LEFT, PP_ALIGN.CENTER])

    box(s, M + 5.95, 1.8, 5.95, 2.25, fill=WHITE)
    text(s, M + 6.2, 1.94, 5.4, 0.28, "WORKED EXAMPLE", 9, True, ACCENT, "Consolas")
    text(s, M + 6.2, 2.28, 5.5, 0.55,
         "\u201cBlack Dell laptop charger\u201d  (lost)\nvs  \u201cDell 65W charger with black cable\u201d  (found)",
         10.5, True, TEXT, spacing=1.3)
    text(s, M + 6.2, 2.95, 5.45, 0.5,
         "40 + 30 + 20 + 10  =  **100 / 100  ·  Very High**", 11.5, False, SUCCESS)
    text(s, M + 6.2, 3.42, 5.45, 0.45,
         "Same category, same lab, one day apart, title keyword matches.",
         9.5, False, MUTED, italic=True)

    box(s, M, 4.3, 5.55, 1.95, fill=WHITE)
    text(s, M + 0.24, 4.44, 5.0, 0.28, "CANDIDATE FILTER", 9, True, ACCENT_DARK, "Consolas")
    bullets(s, M + 0.24, 4.78, 5.05, [
        "same category_id",
        "same location_id",
        "event dates within 30 days",
        "both items still OPEN or MATCHED",
    ], size=10, gap=5)

    box(s, M + 5.95, 4.3, 5.95, 1.95, fill=WHITE)
    text(s, M + 6.2, 4.44, 5.4, 0.28, "CONFIDENCE BANDS", 9, True, ACCENT_DARK, "Consolas")
    text(s, M + 6.2, 4.78, 5.45, 1.3,
         "≥ 90  Very High\n≥ 80  High\n≥ 70  Medium\n< 70  Low",
         10.5, False, TEXT, "Consolas", spacing=1.25)

    text(s, M, 6.42, CW, 0.35,
         "The list is a view: computed on demand, never stale, no refresh job to maintain.",
         10.5, False, MUTED, italic=True)

    notes(s, """
The pairing happens in one SQL view, not in Java loops. The database is better at
joining thousands of rows, and we can tune the weights by editing one view. The UI
explains the score in words so a staff member can judge it.
""")


# ----------------------------------------------------------------------------
#  19. Login and roles
# ----------------------------------------------------------------------------
def slide_19():
    s = new_slide()
    chrome(s, "Module 1: login and roles", "Authentication with hashed passwords and role-based authorisation", 19)

    bullets(s, M, 1.78, 6.0, [
        "Passwords stored as **SHA-256 digests**, never plain text.",
        "Identical error for unknown e-mail and wrong password — **no account enumeration**.",
        "Session held in `SessionContext`, cleared on sign-out.",
        "Authorisation enforced **twice**: hidden buttons and a service-level role check.",
    ], size=12.5, gap=14)

    data = [
        ["Action", "Student", "Staff / Admin"],
        ["Report, search, claim", "Yes", "Yes"],
        ["View own claims", "Yes", "Yes"],
        ["View all claims", "No", "Yes"],
        ["Approve / reject / recover", "No", "Yes"],
    ]
    add_table(s, M + 6.3, 1.78, 5.6, data, [0.5, 0.22, 0.28],
              row_h=0.52, size=10.5, aligns=[PP_ALIGN.LEFT, PP_ALIGN.CENTER, PP_ALIGN.CENTER])

    box(s, M, 4.60, 6.0, 1.30, fill=WHITE)
    text(s, M + 0.26, 4.72, 5.4, 0.28, "DEFENCE IN DEPTH", 9, True, ACCENT, "Consolas")
    text(s, M + 0.26, 5.04, 5.5, 0.8,
         "The UI hides the verification buttons. `ClaimService.requireVerifier()` throws "
         "if a student calls the service directly. The database unique key blocks the rest.",
         10, False, TEXT, spacing=1.25)

    box(s, M + 6.3, 4.58, 5.6, 1.38, fill=WHITE)
    text(s, M + 6.55, 4.70, 5.1, 0.28, "WHAT HAPPENS ON LOGIN", 9, True, ACCENT_DARK, "Consolas")
    text(s, M + 6.55, 5.02, 5.15, 0.85,
         "digest = SHA-256(password)\n"
         "row    = SELECT * FROM users WHERE email = ?\n"
         "        MessageDigest.isEqual(digest, row.hash)\n"
         "        ->  SessionContext.setCurrentUser(row)",
         9, False, TEXT, "Consolas", spacing=1.35)

    notes(s, """
Authorisation is enforced twice: the UI hides the verification buttons, and
ClaimService.requireVerifier() throws if a student calls it directly.
""")


# ----------------------------------------------------------------------------
#  20-22, 24. Module slides with screenshot slot
# ----------------------------------------------------------------------------
def module_slide(number, title, subtitle, points, slot_title, extra=None):
    s = new_slide()
    chrome(s, title, subtitle, number)
    bullets(s, M, 1.78, 5.35, points, size=11.5, gap=12)
    screenshot_slot(s, 6.35, 1.76, 6.25, 4.35, slot_title)
    if extra:
        text(s, M, 5.9, 5.35, 0.9, extra, 9.5, False, MUTED, italic=True, spacing=1.3)
    return s


def slide_20():
    s = module_slide(
        20, "Module 2: reporting an item",
        "One form, two shapes — the visual equivalent of the Item hierarchy",
        [
            "The type switch changes **only** the type-specific fields (`CardLayout`).",
            "**Lost** → contact, reward, urgency.",
            "**Found** → storage location, handed to security, finder notes.",
            "Validation in `ItemService`: lengths, required lookups, no future date, reward ≥ 0.",
            "One report = **two INSERTs in one transaction** (`items` + subtype row).",
        ],
        "Report an item form",
        "The service receives either a `LostItem` or a `FoundItem` through the same method.")
    notes(s, """
This is where the OOPS hierarchy pays off in the UI: the form literally swaps panels,
and the service receives either a LostItem or a FoundItem through the same method.
""")


def slide_21():
    s = module_slide(
        21, "Module 3: search and filter",
        "Five filters, one parameterised query", [
            "Filters: **keyword · category · location · report type · status**.",
            "Any combination, executed as **one dynamic SQL query**.",
            "`ItemFilter` carries the criteria from UI to DAO.",
            "`PreparedStatement` binding → **SQL-injection safe**.",
            "Double-click a row → full details → submit a claim.",
        ],
        "Search results with filters",
        "Every value is bound, never concatenated — that is the difference between a demo and a safe application.")
    notes(s, """
The DAO builds the WHERE clause dynamically but every value is bound as a parameter.
Nothing from the user is ever concatenated into SQL.
""")


def slide_22():
    s = module_slide(
        22, "Module 4: match suggestions",
        "Scored pairs with a plain-English reason", [
            "Rows are **lost ↔ found pairs**, highest score first.",
            "Confidence: **Very High ≥ 90 · High ≥ 80 · Medium ≥ 70**.",
            "Selecting a row explains the score in words.",
            "Double-click opens the found item to submit a claim.",
            "**Read-only report**: computed live, nothing stored, nothing stale.",
        ],
        "Match suggestions table",
        "A new report appears in the list immediately — no nightly job, no cache to invalidate.")
    notes(s, """
Because the view is computed on demand, a new report appears in the match list
immediately. There is no nightly job and no cache to invalidate.
""")


def slide_24():
    s = module_slide(
        24, "Module 6: reports and analytics",
        "The numbers the college actually needs", [
            "Counters: total reports, **recovery rate**, pending claims, total claims.",
            "Breakdowns: category, item status, claim status, monthly lost vs found.",
            "All figures from SQL `GROUP BY` — no full-table loading into Java.",
            "**CSV export** for the weekly security review (UTF-8 with BOM).",
        ],
        "Reports and analytics screen",
        "The recovery rate is available only because every handover is recorded.")
    notes(s, """
The recovery rate is the number the college actually cares about: recovered items
divided by total reports. It is available because every handover is recorded, which
the paper register cannot do.
""")


# ----------------------------------------------------------------------------
#  23. Claims and recovery
# ----------------------------------------------------------------------------
def slide_23():
    s = new_slide()
    chrome(s, "Module 5: claims and recovery", "Every claim is verified, every state change is audited", 23)

    # state machine
    states = [("PENDING", 2.0, WARNING), ("APPROVED", 5.4, ACCENT), ("RECOVERED", 8.8, SUCCESS)]
    for label, x, color in states:
        box(s, x, 1.8, 2.15, 0.72, fill=WHITE, line=color, radius=0.25)
        text(s, x, 1.8, 2.15, 0.72, label, 10.5, True, color, "Consolas",
             align=PP_ALIGN.CENTER, anchor=MSO_ANCHOR.MIDDLE)

    for x in (4.18, 7.58):
        arrow = s.shapes.add_shape(MSO_SHAPE.RIGHT_ARROW, Inches(x), Inches(2.03),
                                   Inches(1.05), Inches(0.24))
        arrow.fill.solid()
        arrow.fill.fore_color.rgb = RGBColor(0xB9, 0xCF, 0xDE)
        arrow.line.fill.background()
        arrow.shadow.inherit = False

    text(s, 4.18, 1.6, 1.05, 0.2, "approve", 6.5, False, MUTED, align=PP_ALIGN.CENTER)
    text(s, 7.58, 1.6, 1.05, 0.2, "handover", 6.5, False, MUTED, align=PP_ALIGN.CENTER)

    box(s, 2.0, 3.0, 2.15, 0.7, fill=RGBColor(0xFD, 0xEC, 0xEA), line=DANGER, radius=0.25)
    text(s, 2.0, 3.0, 2.15, 0.7, "REJECTED", 10.5, True, DANGER, "Consolas",
         align=PP_ALIGN.CENTER, anchor=MSO_ANCHOR.MIDDLE)
    down = s.shapes.add_shape(MSO_SHAPE.DOWN_ARROW, Inches(2.94), Inches(2.58),
                              Inches(0.28), Inches(0.36))
    down.fill.solid()
    down.fill.fore_color.rgb = RGBColor(0xE0, 0xBA, 0xB5)
    down.line.fill.background()
    down.shadow.inherit = False
    text(s, 3.3, 2.62, 2.0, 0.25, "reject → item stays OPEN", 7.5, False, MUTED)

    bullets(s, M, 4.12, 6.1, [
        "Proof of ownership is **mandatory and stored**.",
        "Approving one claim **auto-rejects** the competing claims.",
        "Every status change is logged by the **trigger** into `item_status_history`.",
        "Staff see all claims; a student sees only their own (privacy).",
    ], size=11.5, gap=10)

    screenshot_slot(s, 7.1, 3.72, 5.5, 2.5, "Claims verification screen")

    notes(s, """
Notice the workflow is enforced on both sides: the item moves to CLAIMED on approval,
so a second claimant cannot file, and the unique key blocks a duplicate claim anyway.
""")


# ----------------------------------------------------------------------------
#  25. UI tour
# ----------------------------------------------------------------------------
def slide_25():
    s = new_slide()
    chrome(s, "User interface tour", "Six screens, one theme, consistent behaviour", 25)

    slots = [
        ("Dashboard", "Live counters + recent reports"),
        ("Report an item", "Dynamic lost / found form"),
        ("Search and claim", "Filtered table + detail dialog"),
        ("Match suggestions", "Scored pairs + explanation"),
        ("Ownership claims", "Approve / reject / recover"),
        ("Reports and analytics", "GROUP BY summaries + CSV"),
    ]
    for i, (title, caption) in enumerate(slots):
        x = M + (i % 3) * 4.09
        y = 1.78 + (i // 3) * 2.42
        screenshot_slot(s, x, y, 3.7, 2.15, title, caption)

    notes(s, """
All six screens share one style class, so the application looks like a product rather
than six separate assignments. Every table is sortable and refreshes on navigation.
""")


# ----------------------------------------------------------------------------
#  26. Testing
# ----------------------------------------------------------------------------
def slide_26():
    s = new_slide()
    chrome(s, "Testing", "Manual functional tests plus SQL-level verification", 26)

    data = [
        ["#", "Test case", "Expected result", "Status"],
        ["1", "Login with valid credentials", "Dashboard opens with live counters", "Pass"],
        ["2", "Login with wrong password", "\u201cInvalid e-mail address or password\u201d", "Pass"],
        ["3", "Register an existing e-mail", "Rejected with a clear message", "Pass"],
        ["4", "Report with a short title", "Inline validation error", "Pass"],
        ["5", "Report with a future date", "Blocked by spinner maximum and service", "Pass"],
        ["6", "Search category + location", "Only matching rows returned", "Pass"],
        ["7", "Claim your own report", "Claim form hidden", "Pass"],
        ["8", "Submit the same claim twice", "Blocked by service and unique key", "Pass"],
        ["9", "Approve a claim", "Item CLAIMED, competing claims REJECTED", "Pass"],
        ["10", "Student calls approve directly", "ValidationException raised", "Pass"],
        ["11", "Export CSV and open in Excel", "Correct columns, readable characters", "Pass"],
        ["12", "Stop MySQL and start the app", "Friendly connection message", "Pass"],
    ]
    add_table(s, M, 1.72, CW, data, [0.05, 0.32, 0.48, 0.15],
              row_h=0.335, size=9.2, aligns=[PP_ALIGN.CENTER, PP_ALIGN.LEFT, PP_ALIGN.LEFT, PP_ALIGN.CENTER])

    text(s, M, 6.24, CW, 0.3,
         "Full 18-case table in docs/SETUP_AND_DEMO.md §9. Cases 9 and 10 are the important ones.",
         9.5, False, MUTED, italic=True)

    notes(s, """
Testing was manual plus SQL-level checks. The two most important cases are nine and
ten: the transaction is atomic, and authorisation does not depend on the UI.
""")


# ----------------------------------------------------------------------------
#  27. Results
# ----------------------------------------------------------------------------
def slide_27():
    s = new_slide()
    chrome(s, "Results", "Measured on the seeded database", 27)

    cards = [
        ("14", "reports seeded (7 lost / 7 found)", ACCENT),
        ("5", "ownership claims in every state", WARNING),
        ("6", "suggested matches from real data", ACCENT_DARK),
        ("0", "inconsistent states after rollback tests", SUCCESS),
    ]
    for i, (value, caption, color) in enumerate(cards):
        stat(s, M + i * 3.0, 1.85, 2.82, value, caption, color)

    box(s, M, 3.9, CW, 2.0, fill=WHITE)
    text(s, M + 0.35, 4.08, 4.0, 0.3, "VALUE DELIVERED", 9, True, ACCENT, "Consolas")
    text(s, M + 0.35, 4.42, CW - 0.8, 1.3,
         "A searchable, verifiable, auditable replacement for the notice board and the paper "
         "register. Approving a claim moves three rows in one transaction; the dashboard "
         "recovery rate and the item status update together, and the trigger records why.",
         12.5, False, TEXT, spacing=1.35)

    text(s, M, 6.15, CW, 0.4,
         "Recovery rate = recovered items ÷ total reports — visible live, exportable on demand.",
         10.5, False, MUTED, italic=True)

    notes(s, """
In the demo you will see the numbers change: approve a claim, then the dashboard
recovery rate and the item status update together.
""")


# ----------------------------------------------------------------------------
#  28. Limitations and future scope
# ----------------------------------------------------------------------------
def slide_28():
    s = new_slide()
    chrome(s, "Limitations and future scope", "Honest about what it is not — specific about what comes next", 28)

    box(s, M, 1.8, 5.85, 4.0, fill=WHITE)
    text(s, M + 0.3, 1.96, 4.0, 0.3, "CURRENT LIMITATIONS", 9.5, True, WARNING, "Consolas")
    bullets(s, M + 0.3, 2.36, 5.25, [
        "Desktop-only, single campus.",
        "No photo attachment — text descriptions only.",
        "SHA-256 without a salt: adequate for a project, not production.",
        "Matching is rule-based, not machine-learned.",
        "Categories and locations are maintained in SQL, not in the UI.",
    ], size=11, gap=11, bullet_color=WARNING)

    box(s, M + 6.05, 1.8, 5.85, 4.0, fill=WHITE)
    text(s, M + 6.35, 1.96, 4.0, 0.3, "NEXT STEPS", 9.5, True, SUCCESS, "Consolas")
    bullets(s, M + 6.35, 2.36, 5.25, [
        "Photo upload + image similarity search.",
        "E-mail / SMS notification on a new match.",
        "Web or mobile client on the same database.",
        "QR-code tags on lab equipment.",
        "Salted BCrypt hashing and an audit login table.",
    ], size=11, gap=11, bullet_color=SUCCESS)

    text(s, M, 6.05, CW, 0.5,
         "Each item is a well-scoped extension, not a rewrite — because the layers are separated.",
         11.5, False, MUTED, italic=True)

    notes(s, """
We are explicit about the limits. Each one is a well-scoped extension rather than a
rewrite, because the layers are separated.
""")


# ----------------------------------------------------------------------------
#  29. Conclusion
# ----------------------------------------------------------------------------
def slide_29():
    s = new_slide()
    chrome(s, "Conclusion", None, 29)

    text(s, M, 1.75, CW, 1.0,
         "One database, one workflow,\nzero lost-and-found chaos.",
         30, True, NAVY, spacing=1.25)

    rows = [
        ("Problem", "Fragmented, unsearchable, unverifiable reporting."),
        ("Solution", "A layered Java + MySQL application with verified recovery."),
        ("Learning", "OOPS and DBMS applied where they genuinely fit — inheritance for item "
                     "types, transactions for claims, views for matching."),
    ]
    for i, (label, body) in enumerate(rows):
        y = 3.45 + i * 1.12
        text(s, M, y, 1.5, 0.3, label.upper(), 9, True, ACCENT, "Consolas")
        text(s, M + 1.65, y - 0.05, CW - 1.7, 0.85, body, 12.5, False, TEXT, spacing=1.3)
        if i < 2:
            line = s.shapes.add_connector(MSO_CONNECTOR.STRAIGHT, Inches(M + 1.65),
                                          Inches(y + 0.88), Inches(W - M), Inches(y + 0.88))
            line.line.color.rgb = RGBColor(0xE6, 0xEC, 0xF2)
            line.line.width = Pt(1)

    notes(s, """
To close: the project solves a real campus problem and, more importantly for this
review, every concept we studied has a concrete job inside it.
""")


# ----------------------------------------------------------------------------
#  30. Thank you
# ----------------------------------------------------------------------------
def slide_30():
    s = new_slide(NAVY)

    oval = s.shapes.add_shape(MSO_SHAPE.OVAL, Inches(9.2), Inches(3.4), Inches(5.6), Inches(5.6))
    oval.fill.solid()
    oval.fill.fore_color.rgb = RGBColor(0x9F, 0xC9, 0xDD)
    oval.line.fill.background()
    oval.shadow.inherit = False
    solid = oval.fill._xPr.find(qn("a:solidFill"))
    clr = solid.find(qn("a:srgbClr"))
    clr.append(clr.makeelement(qn("a:alpha"), {"val": "6000"}))

    text(s, M, 2.15, 8.6, 0.9, "Thank you", 48, True, WHITE)
    text(s, M, 3.05, 8.6, 0.5, "Questions?", 22, False, RGBColor(0xC9, 0xD6, 0xE2))

    s.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(M + 0.03), Inches(3.78),
                       Inches(1.55), Inches(0.07)).fill.solid()
    rule = s.shapes[-1]
    rule.fill.fore_color.rgb = ACCENT
    rule.line.fill.background()

    text(s, M, 4.15, 8.2, 0.9,
         "Happy to demonstrate any screen live — including the transaction\nbehind the approve button.",
         13.5, False, RGBColor(0xC9, 0xD6, 0xE2), spacing=1.35)

    text(s, M, 5.55, 9.2, 0.3, "DEMO ACCOUNTS", 8.5, True, RGBColor(0x6E, 0x8C, 0xA8))
    text(s, M, 5.85, 9.5, 0.6,
         "security@campus.edu / staff123     ·     arjun@campus.edu / student123",
         12, True, TEAL, "Consolas")

    text(s, W - M - 3.4, 7.02, 3.4, 0.25, "Campus Lost & Found  ·  2025–26",
         9, False, RGBColor(0x6E, 0x8C, 0xA8), align=PP_ALIGN.RIGHT)

    notes(s, """
We are happy to take questions, and we can demonstrate any screen live - including the
database transaction behind the approve button.
""")


# ----------------------------------------------------------------------------
#  Build
# ----------------------------------------------------------------------------
def build():
    for builder in [
        slide_01, slide_02, slide_03, slide_04, slide_05, slide_06, slide_07, slide_08,
        slide_09, slide_10, slide_11, slide_12, slide_13, slide_14, slide_15, slide_16,
        slide_17, slide_18, slide_19, slide_20, slide_21, slide_22, slide_23, slide_24,
        slide_25, slide_26, slide_27, slide_28, slide_29, slide_30,
    ]:
        builder()

    props = prs.core_properties
    props.title = "Campus Lost & Found Management System"
    props.subject = "OOPS + DBMS mini project presentation"
    props.author = "Campus Lost & Found project"
    props.keywords = "Java, Swing, JDBC, MySQL, OOPS, DBMS"
    props.comments = "Generated from docs/PPT_SLIDES.md"

    out = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                       "Campus_Lost_and_Found_Presentation.pptx")
    prs.save(out)
    print("saved:", out)
    print("slides:", len(prs.slides.__iter__.__self__._sldIdLst))


if __name__ == "__main__":
    build()
