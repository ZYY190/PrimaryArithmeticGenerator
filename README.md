# 小学四则运算自动出题程序

本项目是 Java 17 命令行程序，用于生成小学四则运算题目、标准答案，并对已有答案文件进行判分。项目提供可重复运行的构建脚本、自测试入口、1 万道题测试、性能采样和完整设计文档。

GitHub 项目地址：<https://github.com/ZYY190/PrimaryArithmeticGenerator>

## 功能

- `-n` 控制生成题目数量，`-r` 控制题中数值范围，满足 `数值 < r`。
- 支持自然数、真分数、带分数，输出格式包括 `3/5`、`1’1/2`。
- 每道题包含 1 至 3 个运算符，支持加、减、乘、除和括号。
- 任意减法子表达式的左值不小于右值，不会产生负数中间结果。
- 任意除法子表达式的结果为真分数。
- 使用表达式规范化键去重，处理加法和乘法的交换律，同时保留结合顺序差异。
- 同时生成 `Exercises.txt` 和 `Answers.txt`。
- 支持 10,000 道题生成。
- `-e` 与 `-a` 模式生成 `Grade.txt` 判分结果。
- 不依赖第三方 Java 库，便于在 IDEA 中直接运行。

## 环境

- JDK 17
- Windows PowerShell 5.1 或更高版本
- 建议使用 IntelliJ IDEA 2023 或更高版本

## 构建

```powershell
.\scripts\build.ps1
```

构建结果：`dist\Myapp.jar`。

## 生成题目

```powershell
.\Myapp.cmd -n 10 -r 10
```

也可以直接运行 JAR：

```powershell
java -jar .\dist\Myapp.jar -n 10 -r 10
```

## 判分

```powershell
.\Myapp.cmd -e Exercises.txt -a Answers.txt
```

判分结果写入 `Grade.txt`：

```text
Correct: 9 (1, 2, 3, 4, 5, 6, 7, 8, 10)
Wrong: 1 (9)
```

## 参数

| 参数 | 说明 |
| --- | --- |
| `-n` | 题目数量，正整数 |
| `-r` | 数值范围，所有自然数、分子和分母均小于该值，正整数 |
| `-e` | 判分模式中的题目文件 |
| `-a` | 判分模式中的答案文件 |
| `-h` | 显示帮助 |

## 测试

```powershell
.\scripts\run-tests.ps1
```

测试覆盖分数格式、解析器、括号、交换律去重、结合顺序区分、生成约束、1 万道题生成和判分统计。

## 性能

```powershell
java -cp .\build\classes com.zyy.arithmetic.BenchmarkSuite .\docs\data\performance-runs.csv 100
java -Xmx1g -cp .\build\classes com.zyy.arithmetic.ProfilingRunner 300000 100 .\docs\data\profile-methods.csv
```

实测 100,000 道题三次运行的中位耗时为 231 ms，详细数据见 `docs/performance.md`。

## 在 IDEA 中运行

1. 使用 IDEA 打开项目根目录。
2. 将 `src/main/java` 标记为 Sources Root，将 `src/test/java` 标记为 Test Sources Root。
3. 确保 Project SDK 为 JDK 17。
4. 运行 `com.zyy.arithmetic.Main`。
5. 在 Run Configuration 的 Program arguments 中填写 `-n 10 -r 10`，或填写 `-e Exercises.txt -a Answers.txt`。

## 项目结构

```text
src/main/java/com/zyy/arithmetic/   核心源码
src/test/java/com/zyy/arithmetic/   自测试入口
scripts/                            构建、测试、性能与图表示例脚本
docs/                               设计、PSP、测试、性能与博客草稿
examples/                           示例题目、答案和判分结果
dist/                               可运行 JAR
```
