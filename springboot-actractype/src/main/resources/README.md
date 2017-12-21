###########################################################################################################
                               ####   快速搭建平台 项目模版 阅读指南  ####
###########################################################################################################
一:什么是Archetype
原型是一个Maven项目模板工具包。一个原型被定义为从其中相同类型的所有其它事情是由一个原始图案或模型。名称配合，
因为我们正在努力提供一种系统，该系统提供了一种生成Maven项目的一致的手段。原型将帮助作者为用户创建Maven项目模板，
并为用户提供了手段，产生的这些项目模板参数化的版本。

二:搭建流程
#####1.构建基本项目
        首先创建一个基本的maven项目，quickStart、webapp 等，加上自定义的配置文件等相关配置及基本jar包，这里泛指公共
        部分，毕竟创建项目模版不会太依赖业务
#####2.执行mvn命令 构建项目模版
        创建完基本项目后，在项目根目录执行maven命令  `mvn archetype:create-from-project`
        执行完命令后，maven会在基本项目的target目录生成generated-sources 文件夹
       
        修改pom.xml文件
        定义你的骨架项目的maven坐标 (groupId ,artifactId,name,version)
        修改packing的值为jar
        删除scm标签，保留distributionManagement标签
        修改archetype-metadata.xml，在fileSets同级下增加requiredProperties标签。requiredProperties定义模板项目
        中一些参数值，内置的参数包括：groupId,artifactId,version,package。自己可以提供这些参数的默认值，也可以
        增加自己额外的参数。大部分情况下，内置的参数足够用。fileSet标签有一个filtered=“true”属性，表示该fileSet
        可以使用参数变量。在文件中引用参数变量的方式${参数名},例如${package}.通常生成A-template项目时，
        archetype插件会扫描A-min所有的文件，把需要替换为参数的地方自动替换。毕竟代码没有那么智能，有些地方会有问题。
        所以，我们要检查每个文件的参数部分是不是我们预期的。增加参数、删除参数来达到我们的要求。
#####3.编译项目模版到本地仓库
        我们进入generated-sources 文件夹下的archetype 目录，在此目录下执行mvn clean install 编译项目模版，并添加到
        本地仓库，当然如果你要想工作组中使用可以执行 mvn deploy 发布到私服或者指定仓库中
#####4.创建项目引入模版的两种方式
        使用项目模版：
        第一种直接执行maven命令 ：
        在自己的workspace下执行  mvn archetype:generate -DarchetypeCatalog=local  -o 
        选择刚才的模板项目的编号，按照提示输入参数值。这样项目就创建完毕了。如果找不到我们的模板项目，检查一下~/.m2/目录下是否有archetype-catalog.xml文件。如果没有在A-template的根目录执行一下 
        mvn archetype:update-local-catalog -o
        第二种使用IDE编辑器来引入项目
        在构建maven项目的地方添加 archetype 并在之后指定项目模版的位置（即 groupID,artifactId,version)
        添加完成后就可以直接使用了



