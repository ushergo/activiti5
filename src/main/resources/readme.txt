一、入门文档（1、2、3需要同时参考有些地方需要互补才看得懂）：

1、Activiti（一）SpringBoot2集成Activiti6：
https://blog.csdn.net/weihao_/article/details/83241662
2、Activiti（二）简单请假流程实现：
https://blog.csdn.net/weihao_/article/details/83242798
3、基于SSM整合Activiti之请假流程实现（二）：
https://blog.csdn.net/u012377333/article/details/79886058

二、进阶学习参考文档：

https://blog.csdn.net/zjx86320/article/category/6294649


三、概述
    工作流(workflow)
    为了完成一项任务或事情,需要多个参与者同时或流水式共同完成。
    目前应用比较广泛的行业：物流，办公管理系统
    是一个半成品项目,需要做二次开发
解决的问题
    为了实现某个业务目标，利用计算机在多个参与者之间按某种预定规则自动传递文档、信息或者任务，从而提高业务目标效率。
发展史
    jbpm(jboss公司)
    BPM：Business Process Management 业务流程管理
    BPMN：Business Process Modeling and Notation 业务流程建模与标注规范
下载
    官网：http://www.activiti.org
文件夹说明
    database
        操作数据库sql脚本
    docs
        api文档与用户指南
    xsd
        xml约束文件
    libs
        jar包
    wars
        web应用
安装
    流程设计器离线安装(整合到eclipse中)
        把activiti-plugin拷贝到eclipse\dropins中
    流程设计器在线安装
        eclipse --> help --> install new software
        Activiti BPMN 2.0 Designer
        http://activiti.org/designer/update
    需要创建数据库表(24张)
        activiti.mysql55.create.engine.sql
        activiti.mysql55.create.history.sql
        activiti.mysql.create.identity.sql
        直接运行sql文件中的sql即可。

25张表介绍

    Activiti的后台是有数据库的支持，所有的表都以ACT_开头。 第二部分是表示表的用途的两个字母标识。用途也和服务的API对应。

    1) ACT_RE_*: 'RE'表示repository。 仓储数据： 这个前缀的表包含了流程定义和流程静态资源（图片，规则，等等）。

    2) ACT_RU_*: 'RU'表示runtime。 运行过程中的流程数据：这些运行时的表，包含流程实例，任务，变量，异步任务，等运行中的数据。 Activiti只在流程实例执行过程中保存这些数据，在流程结束时就会删除这些记录。 这样运行时表可以一直很小速度很快。

    3) ACT_ID_*: 'ID'表示identity。 权限管理数据：这些表包含身份信息，比如用户，组等等。

    4) ACT_HI_*: 'HI'表示history。 历史的数据：这些表包含历史数据，比如历史流程实例，变量，任务等等。

    5) ACT_GE_*: 通用数据， 用于不同场景下。

    6) ACT_EVT_LOG: 事件日志。


    资源库流程规则表
    　　1) act_re_deployment 部署的流程表

    　　2) act_re_model 流程设计模型部署表

    　　3) act_re_procdef 已部署的流程定义数据表

    运行时数据库表
    　　1) act_ru_execution 运行时流程执行实例表

    　　2) act_ru_identitylink 运行时用户流程关系表，主要存储任务节点与参与者的相关信息

    　　3) act_ru_task 运行时任务节点表

    　　4) act_ru_variable 运行时流程变量数据表，运行中的变量存储位置

    历史数据库表
    　　1) act_hi_actinst 历史节点表

    　　2) act_hi_attachment 历史附件表

    　　3) act_hi_comment 历史意见表

    　　4) act_hi_identitylink 历史流程人员表

    　　5) act_hi_detail 历史详情表，提供历史变量的查询

    　　6) act_hi_procinst 历史流程实例表

    　　7) act_hi_taskinst 历史任务实例表

    　　8) act_hi_varinst 历史变量表

    组织机构表
    　　1) act_id_group 用户组信息表

    　　2) act_id_info 用户扩展信息表

    　　3) act_id_membership 用户与用户组对应信息表

    　　4) act_id_user 用户信息表

    　　这四张表很常见，基本的组织机构管理，关于用户认证方面建议还是自己开发一套，组件自带的功能太简单，使用中有很多需求难以满足

    通用数据表
    　　1) act_ge_bytearray 二进制数据表，部署的流程图，就是通用的流程定义和流程资源

    　　2) act_ge_property 属性数据表存储整个流程引擎级别的数据,初始化表结构时，会默认插入三条记录



项目中引入jar包
    第一种方式
        拷贝jar包
    第二种方式
        maven方式
核心类
    ProcessEngineConfiguration
        流程引擎配置信息对象
    ProcessEngine
        流程引擎
    RepositoryService 
        仓储服务
    RuntimeService
        运行时服务
    TaskService
        任务服务
    IdentityService
        身份管理服务
    HistoryService 
        历史服务
    FormService
        表单数据服务
    ManagementService
        管理服务
ProcessEngineConfiguration(流程引擎配置信息对象)创建方式
    第一种
        createProcessEngineConfigurationFromResourceDefault()
            需要加载src/activiti.cfg.xml
            bean的id名称一定为：processEngineConfiguration
    第二种
        createProcessEngineConfigurationFromResource(String resource)
            需要加载src/activiti.cfg.xml(配置文件可以改名)
            bean的id名称一定为：processEngineConfiguration
    第三种
        createProcessEngineConfigurationFromResource(String resource, String beanName)
            需要加载src/activiti.cfg.xml(配置文件可以改名)
            bean的id名：processEngineConfiguration(可以自己给名字)
    第四种
        createProcessEngineConfigurationFromInputStream(InputStream inputStream)
            需要加载src/activiti.cfg.xml(配置文件可以改名)
            bean的id名称一定为：processEngineConfiguration
    第五种
        createProcessEngineConfigurationFromInputStream(InputStream inputStream, String beanName)
            需要加载src/activiti.cfg.xml(配置文件可以改名)
            bean的id名：processEngineConfiguration(可以自己给名字)
    第六种
        createStandaloneProcessEngineConfiguration().
说明
    流程：指完成某件事情需要的过程.
    流程定义：具体的流程.把具体流程定义在bpmn文件中.
    流程部署：把流程定义文件保存到数据库.
Activiti5:工作流
1. RepositoryService仓储服务：
   -- act_ge_bytearray
   -- act_re_deployment
   -- act_re_procdef
   -- act_ge_property

   完成的功能：
   -- 流程部署
   -- 流程部署查询
   -- 删除流程部署
   -- 流程定义查询
   -- 查询流程定义资源文件(BpmnModel)
   -- 查询流程定义资源图片(InputStream)

2. RuntimeService运行时服务:
   -- act_ru_execution
   -- act_ru_event_subscr
   -- act_ru_job
   -- act_ru_task
   -- act_ru_variable

   完成的功能：
   -- 开启流程实例.
   -- 查询流程实例.
   -- 删除流程实例.
   -- 触发走一步.signal
   -- 获取当前活动的节点.

3. TaskService任务服务:
   -- act_ru_task
   完成的功能:
   -- 查询任务.
   -- 完成任务.
  
4. HistoryService历史服务:
   -- act_hi_procinst
   -- act_hi_taskinst

   完成的功能：
   -- 查询历史流程实例
   -- 查询历史任务实例