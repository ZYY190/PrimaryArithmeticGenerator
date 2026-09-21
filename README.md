# 小学四则运算自动出题程序

Java 17 命令行程序，用于生成小学四则运算题目、标准答案，并对答案文件进行判分。

## 快速运行

```powershell
.\scripts\build.ps1
.\Myapp.cmd -n 10 -r 10
```

程序会在当前目录生成 `Exercises.txt` 和 `Answers.txt`。

判分模式：

```powershell
.\Myapp.cmd -e Exercises.txt -a Answers.txt
```

判分结果写入 `Grade.txt`。
