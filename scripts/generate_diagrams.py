from __future__ import annotations

import csv
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "docs" / "data"
IMAGES = ROOT / "docs" / "images"
IMAGES.mkdir(parents=True, exist_ok=True)

FONT_CANDIDATES = [
    Path(r"C:\Windows\Fonts\msyh.ttc"),
    Path(r"C:\Windows\Fonts\msyhbd.ttc"),
    Path(r"C:\Windows\Fonts\simhei.ttf"),
]
MONO_CANDIDATES = [Path(r"C:\Windows\Fonts\consola.ttf"), Path(r"C:\Windows\Fonts\cour.ttf")]


def font(size: int, bold: bool = False):
    candidates = FONT_CANDIDATES[1:2] + FONT_CANDIDATES[:1] if bold else FONT_CANDIDATES
    for path in candidates + FONT_CANDIDATES:
        if path.exists():
            return ImageFont.truetype(str(path), size)
    return ImageFont.load_default()


def mono(size: int):
    for path in MONO_CANDIDATES:
        if path.exists():
            return ImageFont.truetype(str(path), size)
    return font(size)


def text(draw, xy, value, fill="#172033", size=26, bold=False, anchor=None):
    draw.text(xy, value, font=font(size, bold), fill=fill, anchor=anchor)


def center(draw, box, value, fill="#172033", size=26, bold=False):
    x1, y1, x2, y2 = box
    draw.multiline_text(
        ((x1 + x2) / 2, (y1 + y2) / 2),
        value,
        font=font(size, bold),
        fill=fill,
        anchor="mm",
        align="center",
        spacing=8,
    )


def arrow(draw, start, end, color="#4b5d77", width=4, dashed=False):
    if dashed:
        x1, y1 = start
        x2, y2 = end
        length = max(abs(x2 - x1), abs(y2 - y1))
        steps = max(1, int(length // 18))
        for index in range(0, steps, 2):
            t1 = index / steps
            t2 = min(1.0, (index + 1) / steps)
            draw.line(
                [(x1 + (x2 - x1) * t1, y1 + (y2 - y1) * t1),
                 (x1 + (x2 - x1) * t2, y1 + (y2 - y1) * t2)],
                fill=color,
                width=width,
            )
    else:
        draw.line([start, end], fill=color, width=width)
    x1, y1 = start
    x2, y2 = end
    angle_vectors = [(14, 0), (-14, 0), (0, 14), (0, -14)]
    if abs(x2 - x1) >= abs(y2 - y1):
        direction = (1 if x2 > x1 else -1, 0)
    else:
        direction = (0, 1 if y2 > y1 else -1)
    if direction[0]:
        points = [(x2, y2), (x2 - direction[0] * 18, y2 - 9), (x2 - direction[0] * 18, y2 + 9)]
    else:
        points = [(x2, y2), (x2 - 9, y2 - direction[1] * 18), (x2 + 9, y2 - direction[1] * 18)]
    draw.polygon(points, fill=color)


def node(draw, box, label, kind="process", size=25):
    palette = {
        "start": ("#d7f5e5", "#2f8f5b"),
        "process": ("#e8f0ff", "#3f68b5"),
        "decision": ("#fff3d6", "#c68416"),
        "io": ("#f3e8ff", "#7b4bb7"),
        "fail": ("#ffe5e5", "#c34848"),
    }
    fill, outline = palette[kind]
    x1, y1, x2, y2 = box
    if kind in {"start", "fail"}:
        draw.rounded_rectangle(box, radius=(y2 - y1) // 2, fill=fill, outline=outline, width=3)
    elif kind == "decision":
        points = [((x1 + x2) / 2, y1), (x2, (y1 + y2) / 2), ((x1 + x2) / 2, y2), (x1, (y1 + y2) / 2)]
        draw.polygon(points, fill=fill, outline=outline)
        draw.line(points + [points[0]], fill=outline, width=3)
    else:
        draw.rounded_rectangle(box, radius=18, fill=fill, outline=outline, width=3)
    center(draw, box, label, size=size)


def draw_flow(path: Path, title: str, steps: list[tuple[str, str]]):
    canvas = Image.new("RGB", (1500, 1900), "#ffffff")
    draw = ImageDraw.Draw(canvas)
    text(draw, (750, 54), title, anchor="mm", size=44, bold=True)
    y = 120
    boxes = []
    for label, kind in steps:
        height = 108 if kind == "decision" else 90
        width = 900 if kind == "decision" else 780
        box = ((1500 - width) // 2, y, (1500 + width) // 2, y + height)
        node(draw, box, label, kind)
        boxes.append((box, kind))
        y += height + 62
    for index in range(len(boxes) - 1):
        box, kind = boxes[index]
        next_box, _ = boxes[index + 1]
        arrow(draw, ((box[0] + box[2]) // 2, box[3]), ((next_box[0] + next_box[2]) // 2, next_box[1]))
    canvas.save(path, "PNG")
    canvas.close()


def generate_flowcharts():
    draw_flow(
        IMAGES / "generation-flow.png",
        "题目生成流程",
        [
            ("开始", "start"),
            ("解析 -n 与 -r，校验参数", "process"),
            ("初始化随机数、唯一题目集合", "process"),
            ("随机生成 1 至 3 个运算符的表达式树", "process"),
            ("检查减法非负、除法结果为正真分数", "decision"),
            ("计算规范化表达式键，检查是否重复", "decision"),
            ("加入题目集合，序号加 1", "process"),
            ("题目数量达到 -n 要求？", "decision"),
            ("写入 Exercises.txt 与 Answers.txt", "io"),
            ("结束", "start"),
        ],
    )
    draw_flow(
        IMAGES / "grading-flow.png",
        "答案判分流程",
        [
            ("开始", "start"),
            ("解析 -e 与 -a，读取两个 UTF-8 文件", "io"),
            ("题目数与答案数一致？", "decision"),
            ("逐行去掉题号，解析表达式与答案", "process"),
            ("比较两个分数是否相等", "decision"),
            ("记录正确或错误题号", "process"),
            ("统计数量并生成 Grade.txt", "io"),
            ("结束", "start"),
        ],
    )


def architecture():
    canvas = Image.new("RGB", (1700, 1100), "#ffffff")
    draw = ImageDraw.Draw(canvas)
    text(draw, (850, 55), "程序模块与类关系", anchor="mm", size=46, bold=True)

    def block(box, label, color="#e8f0ff", outline="#3f68b5", size=26):
        draw.rounded_rectangle(box, radius=18, fill=color, outline=outline, width=3)
        center(draw, box, label, size=size)

    blocks = {
        "Main": (650, 140, 1050, 245),
        "CliOptions": (80, 390, 440, 500),
        "ArithmeticGenerator": (520, 390, 920, 500),
        "Grader": (1000, 390, 1390, 500),
        "Expression": (340, 680, 720, 790),
        "Fraction": (780, 680, 1080, 790),
        "ExpressionParser": (1140, 680, 1530, 790),
        "FileService": (1120, 880, 1530, 980),
        "PerformanceRunner": (80, 680, 300, 790),
    }
    for name, box in blocks.items():
        block(box, name)
    arrow(draw, (850, 245), (260, 390))
    arrow(draw, (850, 245), (720, 390))
    arrow(draw, (850, 245), (1195, 390))
    arrow(draw, (720, 500), (530, 680))
    arrow(draw, (720, 500), (930, 680))
    arrow(draw, (1195, 500), (1335, 680))
    arrow(draw, (1195, 500), (1325, 880))
    arrow(draw, (190, 790), (620, 500))
    text(draw, (850, 1020), "实线表示主要调用或依赖关系", anchor="mm", size=25)
    canvas.save(IMAGES / "architecture.png", "PNG")
    canvas.close()


def load_csv(path: Path):
    with path.open("r", encoding="utf-8-sig", newline="") as handle:
        return list(csv.DictReader(handle))


def performance_chart():
    runs = load_csv(DATA / "performance-runs.csv")
    methods = load_csv(DATA / "profile-methods.csv")
    canvas = Image.new("RGB", (1700, 1250), "#ffffff")
    draw = ImageDraw.Draw(canvas)
    text(draw, (850, 55), "性能分析", anchor="mm", size=46, bold=True)
    text(draw, (850, 100), "Java 17 | Windows | 范围 r = 100 | 每题最多 3 个运算符", anchor="mm", size=25, fill="#53617a")

    chart = (130, 170, 1570, 690)
    draw.rectangle(chart, outline="#ccd5e3", width=3)
    x0, y0, x1, y1 = 210, 230, 1510, 630
    draw.line([(x0, y1), (x1, y1)], fill="#66758c", width=3)
    draw.line([(x0, y0), (x0, y1)], fill="#66758c", width=3)
    counts = [int(row["count"]) for row in runs]
    medians = [int(row["median_ms"]) for row in runs]
    max_y = max(medians) * 1.2
    points = []
    for index, (count, value) in enumerate(zip(counts, medians)):
        x = x0 + (x1 - x0) * index / (len(counts) - 1)
        y = y1 - (y1 - y0) * value / max_y
        points.append((x, y))
    if len(points) > 1:
        draw.line(points, fill="#2f6fd0", width=6, joint="curve")
    for (x, y), count, value in zip(points, counts, medians):
        draw.ellipse((x - 10, y - 10, x + 10, y + 10), fill="#2f6fd0", outline="#ffffff", width=3)
        text(draw, (x, y - 28), f"{value} ms", anchor="mm", size=23, fill="#244b84")
        text(draw, (x, y1 + 26), f"{count:,}", anchor="mm", size=22)
    text(draw, (850, 190), "题目数量与生成耗时（三次运行中位数）", anchor="mm", size=30, bold=True)
    text(draw, (850, 670), "题目数量", anchor="mm", size=24)
    text(draw, (210, 210), "耗时 ms", anchor="mm", size=24)

    method_panel = (130, 760, 1570, 1180)
    draw.rectangle(method_panel, outline="#ccd5e3", width=3)
    text(draw, (850, 800), "热点方法采样占比（300,000 道题）", anchor="mm", size=30, bold=True)
    top = methods[:8]
    left, top_y, right = 700, 865, 1460
    bar_gap = 40
    max_percent = max(float(row["percent"]) for row in top)
    for index, row in enumerate(top):
        y = top_y + index * bar_gap
        width = int((right - left) * float(row["percent"]) / max_percent)
        draw.rounded_rectangle((left, y, left + width, y + 24), radius=10, fill="#73a6e8")
        method = row["method"].replace("com.zyy.arithmetic.", "").replace("java.math.", "java.math.")
        text(draw, (670, y + 12), method, anchor="rm", size=21)
        text(draw, (left + width + 12, y + 12), f"{row['percent']}%", anchor="lm", size=21, fill="#33527c")
    canvas.save(IMAGES / "performance-chart.png", "PNG")
    canvas.close()


if __name__ == "__main__":
    generate_flowcharts()
    architecture()
    performance_chart()
    print("Generated diagrams in", IMAGES)
