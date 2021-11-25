## 5.Gradle 插件

### 1.作用

1.添加任务到项目，测试，编译，打包

2.添加依赖配置

3.向现有的对象类型添加新的扩展属性，帮助我们优化构建，eg:android{} 就是

4.对项目进行约定修改，应用Java插件后，约定src/main/java目录下是源码存放位置，

### 2.如何应用插件

#### 2.1应用二进制插件



二进制插件就是实现了org.gradle.api.Plugin接口的插件，有PluginId

````
apply plugin:'java'
````

'java'是Java插件的plugin Id,他是唯一的。

```
apply plugin:org.gradle.api.plugins.JavaPlugin

因为包是默认导入的，可以去除
apply plugin:JavaPlugin
```

二进制插件一般被打包到jar里独立发布

#### 2.2 应用脚本插件

应用脚本插件就是把这个脚本加载进来，使用from，后跟一个脚本文件，额可以本地，可以网络。

可以把脚本文件，进行分块，分段整理，拆分成共用、职责分明的文件。然后使用apply from来引用

```
apply from:'version.gradle'
```

apply 其他用法

```
void apply(Map<String,?> options);
void apply(Closure closure);
void apply(Action<? supper ObjectConfiguration> action);
```

#### 2.3 第三方发布的插件

第三方发布的作为jar的二进制插件，先在buildscript{}配置其classpath才能使用。

buidlscript｛｝块是构建项目之前，为项目进行前期准备和初始化相关配置依赖的地方。

```
buildscript {
    repositories {
 		google()
        jcenter()
        
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.5.3'
    
    }
}

```

### 3.自定义插件？?

## 6.java Gradle 插件

### 1.如何应用插件

```
apply plugin:'java'
```

Java插件会为你的工程添加有用的默认设置和约定，如源代码位置，单元测试代码位置，资源文件位置。遵循默认就好。

![](C:\Users\chents\Pictures\6-1.png)

src/main/java 源代码

src/main/resources 资源

src/test/java 单元测试源代码

src/test/resources 单元测试资源



main和test 是Java插件内置的两个源代码集合，也可以自定义添加

```
sourceSets{
    vip{
        
    }
}
//这样就会新建vip/java  vip/resources目录

```

默认情况不需要这种目录结构；如果你想改变：

```
sourceSets {
    main {
        java{
            srcDir 'src/java'
        }
        resources {
            srcDir 'src/resources'
        }
    }
}
```



### 2.配置第三方依赖

1.要使用第三方依赖，第一个告诉gradle去哪找到这些依赖，使用什么类型的仓库

```
buildscript {
    repositories {
 
        google()
        mavenCentral()
    }
}
```

有了仓库，告诉gradle依赖什么

group

name

version

```
  dependencies {
  		compile group:'com.android.tools.build',name:'gradle',version:'4.2.2'
  		//也可以写成下面形式
        compile "com.android.tools.build:gradle:4.2.2"
      
    }
```

compile:编译时依赖

testCompile 编译单元测试依赖，不会打包到apk里

| name     | 继承    | 任务           | msg          |
| -------- | ------- | -------------- | ------------ |
| compile  |         | compileJava    | 编译时依赖   |
| runtime  | compile |                | 运行时依赖   |
| archives |         | uploadArchives | 项目发布构建 |



### 3.构建Java项目

gradle中，执行任何操作都是任务驱动。

build任务:编译源码，处理资源，打包jar 编译测试用例，处理测试资源，运行

clean 删除生产的文件

assemble 不执行单元测试，只会编译打包

check 执行单元测试

javadoc生产api文档



### 4.sourceSets概念

sourceSets源集，是Java插件描述管理代码及资源，一个Java源代码和资源文件的集合。sourceSets是一个SourceSetContainer，可以查阅api.

```
name
output.classesDir  编译后目录
output.resourcesDir 编译后资源目录
java Java源文件
java.srcDirs Java源文件所在目录
resources 资源文件
resources.srcDirs 资源文件所在目录

sourceCompatibility 编译Java源文件使用的Java版本
targetCompatibility 编译生成的类的Java版本
```

### 5.多项目构建setting.gradle

```java
rootProject.name = "Gradle_demo"
include ':app1'
include ':app2'
    
project(':app1').projectDir = new File(rootDir,'/app1')
    
```

subproject

可以在project的gradle文件里让所有子项目应用插件

```
subproject{
    apply plugin 'com.android.application'
}
```



### 6.发布构件

如果你的项目是一个库工程，要发布jar给其他人使用，gradle提供了方便的配置发布的功能。发布到本地目录、maven库、Ivy库。

```
task pushlish(type:Jar)

artifacts {
    archives pushlish
}

//发布你的打包文件 到本地
uploadArchives {

    repositories {

        flatDir {
            name 'libs'
            dirs "$projectDir/libs"
        }
    }
}

//发布你的打包文件 到maven仓库
uploadArchives {
    repositories {
        mavenDeployer {
            repository(url: Mavens.SNAPSHOTS_URL) {
                authentication(
                        userName: Mavens.SNAPSHOTS_USERNAME,
                        password: Mavens.SNAPSHOTS_PASSWORD
                )
            }
            pom.project {
                version '1.0-SNAPSHOT'
                artifactId 'widgets'
                groupId 'com.xxx.xx'
                description 'upload AAR'
            }
        }
    }
}

```

uploadArchives是一个upload Task，用于上传我们的构件。

## 7.android Gradle 插件



### 1.插件分类

app插件id: com.android.application

library插件id: com.android.library

test插件id: com.android.test

1.配置应用Android Gradle 插件

```
dependencies {
    classpath "com.android.tools.build:gradle:4.2.2"
    classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.20"

}
```

2.就可以在module使用了

```
apply plugin:' com.android.application'
android {
    compileSdk 31
}
```

android{}是Android 插件提供的一个扩展类型。

### 2.Android 工程

android{}是Android 插件提供的一个扩展类型。对Android Gradle工程进行自定义的配置，实现是com.anroid.build.gradle.AppExtension.是project的一个扩展。可参阅API

```
compileSdkVerison 编译Android SDK的版本
buildToolsVersion Android构建工具的版本，eg:appt、dex工具
defaultConfig 默认配置，是一个ProductFlavor，他允许我们根据不同情况产生多个不同的apk。渠道打包
buildTypes 是一个NamedDomainObjectContainer类型，是域对象。类似sourceSet。包含release/debug等。我们还可以新增构建的类型。release就是一种buildTypes

minifyEnabled 是否混淆
proguardFiles 混淆的管理文件，哪些文件进行混淆

```

### 3.Android Gradle任务

>assemble
>
>check
>
>build
>
>connectedCheck
>
>deviceCheck
>
>lint
>
>install uninstall
>
>clean

## 8.自定义Android Gradle 工程

### 1.defaultConfig默认配置

defaultConfig是一个ProductFlavor，

```
//
defaultConfig {
    applicationId "com.example.gradle_demo"
    minSdk 21
    targetSdk 31
    versionCode 1
    versionName "1.0"

    testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
}
```

applicationId是ProductFlavor的一个属性，用于指定生成APP的包名，默认情况下为null。默认会从AndroidManifest.xml读取。

minSdkVersion是ProductFlavor的一个方法。指定最低支持的Android版本。

targetSdkVersion

testApplicationId 用于配置测试APP的包名

testinstrumentationRunner 用于配置单元测试用的Runner.默认使用android.test.InstrumentationTestRunner

signingConfig 配置默认签名信息，对生成的APP签名

### 2.配置签名信息

一个APP只有在签名后，才能被发布、安装、使用。一般有debug和release两种模式。

```
signingConfigs {
    debug {
        keyAlias keystoreProperties['keyAlias']
        keyPassword keystoreProperties['keyPassword']
        storeFile file(keystoreProperties['storeFile'])
        storePassword keystoreProperties['storePassword']
    }
    release {
        keyAlias keystoreProperties['keyAlias']
        keyPassword keystoreProperties['keyPassword']
        storeFile file(keystoreProperties['storeFile'])
        storePassword keystoreProperties['storePassword']
    }
}
```

### 3.构建的应用类型

applicationIdSuffix

applicationIdSuffix是buildTypes的一个属性，配置基于applicaitonId的后缀。如applicationId为com.example.xx,我们指定applicationIdSuffix为.debug. 那么生成的debug apk的包名为com.example.xx.debug

debuggable

debuggable是buildTypes的一个属性,用于配置是否生成一个可供调试的apk。true或者false

jniDebuggable和debugable类似，配置是否可供调试Jni(C/C++)的apk

multiDexEnabled 用于配置buildTypes是否启动自动拆分多个dex的功能，当app方法总量超过65535个方法的时候使用。

shrinkResources 用于配置是否自动清理未使用的资源。默认为false



### 4.启动混淆

### 5.启动zipalign优化

zipalign是Android为我们提供整理优化apk文件的工具，提高系统和应用的运行效率，更快地读写apk中的资源，降低内存使用。

```
defaultConfig {

	zipalign true
}
```













