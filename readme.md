# Gloc运行说明

## AndroidStudio版本
按目前经验，只能在AndroidStudio 3.2版本下正常加载，4.0版本下许多import信息无法解析。
Android SDK版本15~28。
## 工程目录
确保GlocWifi文件夹所处的目录为：D:\AndroidStudio\AndroidProjects\GlocWifi。否则生成.apk文件时会出现输出路径错误的提示。
初次运行之前单击build目录下的clean project，清除历史输出文件。
## 地图文件导入
APP初次运行后，单击buildmap按钮，会自动在手机根目录中创建Gloc/5文件夹。之后需要手动将地图文件复制到该目录中。map.java中有加载地图的文件名语句，请确保复制的地图文件名与加载文件名一致。
