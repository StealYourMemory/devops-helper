# DEVOPS-HELPER

> 本文档主要介绍`devops-helper`的部署，方便各组可以快速用上自己的`devops-helper`。关于`devops-helper`的功能和使用可以参考：[Devops Helper - 殷华盛 - 紫光云](http://10.0.45.171/display/~yinhuasheng@unicloud.com/Devops+Helper)
>
> 整个文档笔者会以**网络A层**为例来讲解，其他组有不同情况或不懂的配置可直接联系笔者。

## 1. 部署

### 1.1 配置文件

部署`devops-helper`**需要先将项目的配置文件补充完整**，这些配置文件主要涉及各个三方服务，如`gitlab`、`devops`等。这些配置在`src/main/resource/`下，如下图：

![image-20240820204456298](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240820204456298.png)

配置目前共分为7个部分，我们一个个来说。

#### 1.1.1 项目配置

项目配置的文件在`src/main/resource/application-project.yml`。所谓项目就是我们的代码和服务，这是`devops-helper`所有功能的一切基石。

**每一个你需要打包，部署的服务都是一个项目**。

每个项目需要配置的信息分别是**项目名**、**项目的gitlab-url**、**项目的jenkins-url**、**项目的k8s deployment名字**和**项目的k8s deployment yaml中镜像的名字**。

以网络A层为例，下面是一个`application-project.yml`的部分配置：

```yaml
devops-helper:
  project:
    # 项目的配置
    projects:
      - name: core-basic
        gitlab-url: network-core-basic
        jenkins-url: uca-network-core-basic
        k8s-deployment: uca-network-core-basic-deployment
        k8s-deployment-image: harbor-local.unicloudsrv.com/moove/uca-network-core-basic
      - name: slb
        gitlab-url: network-slb
        jenkins-url: uca-network-slb
        k8s-deployment: uca-network-slb-deployment
        k8s-deployment-image: harbor-local.unicloudsrv.com/moove/uca-network-slb
      - name: scheduler
        gitlab-url: uca_iaas_scheduler
        jenkins-url: uca-iaas-scheduler
        k8s-deployment: uca-iaas-scheduler-deployment
        k8s-deployment-image: harbor-local.unicloudsrv.com/moove/uca-iaas-scheduler
```

1. 其中`name`是项目名，给每个项目起个简单短小的名字，页面打包部署的时候，下拉的项目名就是这里配的。注：**项目名需要唯一，不能重复**

![image-20240820210100198](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240820210100198.png)

2. `gitlab-url`其实就是gitlab中该项目的名称，比如上面我们的core-basic服务，它在gitlab中叫`network-core-basic`

   ![image-20240820210546837](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240820210546837.png)

3. `jenkins-url`其实就是jenkins中该项目的名称，比如上面我们的core-basic服务，它在jenkins中叫`uca-network-core-basic`

   ![image-20240820210810861](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240820210810861.png)

4. `k8s-deployment`是该项目对应的k8s-deployment名称，比如对于我们的core-basic服务，它的k8s-deployment就叫`uca-network-core-basic-deployment`

   ![image-20240820211039190](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240820211039190.png)

5. `k8s-deployment-image`是项目的镜像名，比如对于core-basic服务，在它的k8s-deployment中配置的镜像名完整名称叫`harbor-local.unicloudsrv.com/moove/uca-network-core-basic`

   ![image-20240820211340581](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240820211340581.png)

#### 1.1.2 gitlab配置

gitlab配置的文件在`src/main/resource/application-gitlab.yml`，gitlab最小配置只需要配置如下三个内容：

1. 用户名：登录gitlab账户的用户名
2. 密码：登录gitlab账户的密码，**密码需要进行Base64加密**
3. gitlab获取项目分支的url

其中gitlab获取项目分支url的获取方式为：

* 选择一个项目，以网络A层为例，我选择core-basic项目，进入该项目的gitlab页面

  ![image-20240820212238017](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240820212238017.png)

* 点开浏览器的开发人员工具（快捷键F12），选择网络（Network），选择Fetch/XHR

  ![](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240820212436817.png)

* 点击gitlab页面中项目的分支下拉栏，此时网络内应该会多出一条网络请求（如果没有请刷新页面后重试）：

  ![image-20240820212649459](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240820212649459.png)

* 单击点击这条网络请求，可以看到请求的URL路径

  ![image-20240914150030373](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240914150030373.png)

  `http://gitlab.rd.unicloud.com/moove/uni-network/network-core-basic/refs?sort=updated_desc&ref=master&search=`，其中`/moove/uni-network/network-core-basic/refs?sort=updated_desc&ref=master&search=`，就是请求的URL路径，而我们又已知`network-core-basic`是之前在项目配置中配置过的项目`gitlab-url`，因此用`%s`占位符替换项目`gitlab-url`。需要的gitlab获取项目分支url即为：`/moove/uni-network/%s/refs?sort=updated_desc&ref=master&search=` （注：请忽略这个URL的ip和端口）。

  以网络A层为例，一个最小gitlab配置文件为：

  ```yaml
  gitlab:
    user-name: yinhuasheng@unicloud.com
    password: MTIzNDU2
    get-branches-url: /moove/uni-network/%s/refs?sort=updated_desc&ref=master&search=
  ```

  切记，密码是Base64加密后的

#### 1.1.3 jenkins配置

jenkins基本无需额外的配置，本项目使用的jenkins用户名密码是公共的`admin/M00ve`，这个账户应该是有权利访问各项目组的服务。

#### 1.1.4 k8s配置

k8s配置的文件在`src/main/resource/application-k8s.yml`。在讲k8s的配置之前我们先讲下devops-helper如何更新我们k8s-deployment中镜像版本的。

* 对于Rebirth1.0版本，我们知道直接更改k8s-deployment的image上的tag即可，k8s会自动拉取jenkins打包后的镜像，然后自动更新pod的镜像版本。这种情况下其实只需要对k8s服务器执行如下指令即可

  ```shell
  kubectl patch deployment %s --type='json' -p='[{"op": "replace", "path": "/spec/template/spec/containers/0/image", "value":"%s:%s"}]' -n %s
  ```

  其中上述4个`%s`占位符分别会被k8s的deployment、k8s-deployment的image名字，本次要部署的镜像tag和k8s的namespace（这些配置大多都是我们在上面项目配置里配过的）。

* 对于Rebirth2.0版本，每个rebirth2.0环境都有自己的harbor，这个harbor与我们jenkins打包后推入镜像仓库的harbor不同，因此Rebirth2.0无法直接拉取jenkins打包后的镜像。对于Rebirth2.0，我们需要一个基础服务器，**这个基础服务器要求是Rebirth1.0的（具备docker指令，能直接拉取jenkins打包后的镜像）**，devops-helper更新Rebirth2.0服务版本的流程是：

  * 在基础服务器上 执行`docker pull`命令拉取jenkins打包后的镜像
  * 在基础服务器上执行`docker save`指令，将上一步拉取的镜像保存到本地
  * 通过`scp`拷贝，将上一步保存到本地的镜像文件拷贝到要部署的Rebirth2.0环境下的某台k8s服务器上
  * 在Rebirth2.0的k8s上执行 `nerdctl load`指令，加载传过来的镜像文件
  * 在Rebirth2.0的k8s上执行 `nerdctl push`指令，将加载的镜像上传到自己的harbor仓库
  * 在Rebirth2.0的k8s上执行如Rebirth1.0的`kubectl patch deployment`指令，更新k8s-deployment的镜像tag。
  * 清除上面拉取和保存的本地镜像文件，避免浪费服务器硬盘

可以看到对于Rebirth2.0，我们需要一个基础服务器，这个服务器要求是**Rebirth1.0的（具备docker指令，能直接拉取jenkins打包后的镜像）**

下面是一份k8s配置文件的demo：

```yaml
k8s:
  # k8s namespace
  k8s-name-space: default

  # 基础服务器配置
  base-env: NEW_YFB
  base-device-ip: 10.247.146.2
  save-image-path: /home/uca-network/devops-helper/k8s/
  #代理路径
  save-proxy-path: /home/uca-network/devops-helper/proxy/
```

其中上面的基础服务器配置 `base-env: NEW_YFB`、` base-device-ip: 10.247.146.2`就是基础服务器，它负责整个Rebirth2.0的镜像中转。关于环境（ENV）参考[Devops 功能文档](http://10.0.45.171/display/~yinhuasheng@unicloud.com/Devops+Helper)

`k8s-name-space`是服务所在的namespace。这里我将namespace的配置放在了k8s，而非项目配置下。这样就默认一个小组的所有项目都应该在一个namespace下。如果有些组不同项目所在的namespace不同，可自己修改源码或联系笔者适配。

`save-image-path`是上面我们说的基础服务器和Rebirth2.0服务器暂存k8s镜像的目录，每个组的目录镜像最好设置的不一样，别互相影响了，比如对于网络A层，笔者设置的目录是`/home/uca-network/devops-helper/k8s/`。

`save-proxy-path`是服务代理的路径，如果你不需要服务代理功能，可以不用配置，但如果需要，请尽量设置的和别组不一样，避免互相影响。

除此以外，还建议配置k8s的起始配置：

```yaml
k8s:
  # k8s namespace
  k8s-name-space: default

  # 基础服务器配置
  base-env: NEW_YFB
  base-device-ip: 10.247.146.2
  save-image-path: /home/uca-network/devops-helper/k8s/
  #代理路径
  save-proxy-path: /home/uca-network/devops-helper/proxy/

  # 初始化入库的k8s服务器环境
  init:
    init-env-and-server:
      NEW_YFB:
        env-name: NEW_YFB
        env-description: 新预发布
        env-type: REBIRTH_V1
        server-set:
          - ip: 10.247.146.2
            port: 22
            user-name: root
            password: dW5pY2xvdWQ=
          - ip: 10.247.146.4
            port: 22
            user-name: root
            password: dW5pY2xvdWQ=
      CHENGGUI:
        env-name: CHENGGUI
        env-description: 城轨
        env-type: NEED_DOCKER_PULL
        server-set:
          - ip: 10.246.146.152
            port: 22
            user-name: root
            password: dW5pY2xvdWQ=
          - ip: 10.246.146.153
            port: 22
            user-name: root
            password: dW5pY2xvdWQ=
          - ip: 10.246.146.154
            port: 22
            user-name: root
            password: dW5pY2xvdWQ=
      DEVELOP:
        env-name: DEVELOP
        env-description: 开发环境
        env-type: REBIRTH_V2
        server-set:
          - ip: 10.254.7.10
            port: 22
            user-name: root
            password: UGFzc3cwcmRAX1VuaQ==
          - ip: 10.254.7.11
            port: 22
            user-name: root
            password: UGFzc3cwcmRAX1VuaQ==
          - ip: 10.254.7.12
            port: 22
            user-name: root
            password: UGFzc3cwcmRAX1VuaQ==
```

如上init下配置了一些环境和环境下的k8s服务器。其中服务器的密码**需要进行Base64加密**。`env-type`是环境类型，环境类型目前只支持如下三种：

* REBIRTH_V1：Rebirth1.0环境，支持直接更新k8s-deploment的image tag实现自动更新pod版本
* REBIRTH_V2：Rebirth2.0环境，需要借助基础服务器中转
* NEED_DOCKER_PULL：这个是单独为城轨UCA服务器适配的，因为城轨UCA环境其实是Rebirth1.0，但它无法直接更新k8s-deployment实现pod自动更新，需要**先去每个节点上使用docker pull将镜像拉下来**，才能再更新k8s-deployment。

配置k8s.init的好处是：这样项目一启动的时候你就拥有了这些环境和服务器，无需在页面上一个个添加。当然如果你喜欢界面操作，也可以在环境管理界面下添加这些环境和服务器（页面添加服务器输入的密码不要Base64加密，就输原密码）。

![image-20240822173826069](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240822173826069.png)

**注：无论是一开始配置在k8s.init还是后续在页面添加的，一定要有配置的基础服务器！！！**

比如笔者上面配置的基础服务器是环境是`NEW_YFB`，ip是`10.247.146.2`，这是因为我在k8s.init初始化创建了一个**NEW_YFB**的环境和这个环境下创建了一个**10.247.146.2** ip的服务器。



**配置完上述四个配置文件，我们已经可以打包和部署了，如果无需DEVOPS和seafile的功能，则下面的配置可以忽略，直接到Docker部署章节。**

#### 1.1.5 devops配置

devops配置的文件在`src/main/resource/application-devops.yml`。

devops的主要用于需求审查、Bug审查以及归档功能。

Devops的最小配置如下：

```yaml
devops:
  base-url: http://10.0.7.210
  user-name: yinhuasheng@unicloud.com
  password: MTIzNDU2
  login-type: external
  project-id: 446919227341815662
```

其中用户名密码是devops登录的用户名密码，**密码需要进行Base64加密**，login-type是devops的登录方式

![image-20240822174857648](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240822174857648.png)

外部账号是`external`，本地账号是`user`

`project-id`是本项目组的id，它的获取方式如下：

登录Devops，点击项目，选择你的团队，我这里选择**SDN控制器开发团队**：

![image-20240822175140985](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240822175140985.png)

![image-20240822175315487](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240822175315487.png)

上面的URL路径`http://10.201.20.19:31160/console/#/devopscloud/devops/demand/list?redirect=false&projectId=446919227341815662`，其中`446919227341815662`就是本组的`project-id`，你需要按照上述操作，将配置文件改为你们组的`project-id`。

devops的base-url其实就是devops服务的ip+端口，我们可以看到配置文件里配置的是http://10.0.7.210并不是devops本身的http://10.201.20.19:31160。这是因为笔者的`devops-helper`服务部署在`10.247.146.2`服务器上，`10.247.146.2`服务器无法访问通devops服务器`10.201.20.19`，但笔者自己的电脑可以访问通`10.201.20.19`，且`10.247.146.2`可以访问通笔者电脑，因此笔者通过nginx在本地起了一个7层代理，将devops的http://10.201.20.19:31160代理为http://10.0.7.210，其中`10.0.7.210`就是笔者自己电脑的ip。

如果你部署的devops-heler服务器也无法访问通devops的`10.201.20.19`服务，那你也起个代理做中转。如果你部署的devops-heler服务器可以访问通笔者自己的`10.0.7.210`地址，那你也可以**直接用笔者的代理，无需自己再配置代理**。将devops的配置文件base-url设为`http://10.0.7.210`即可。

注1：**代理请代理为80端口**，笔者的代理其实就是将http://10.201.20.19:31160代理为了http://10.0.7.210:80，因为实测下来发现devops有一些重定向，如果不用80，重定向会有挺多问题（也可能是笔者的nginx配置有问题，如果有更好的想法可以一起来讨论）。

注2：受限于公司电脑可能晚上断电等情况，笔者的这个代理可能会不稳定，但好在审查和归档等不属于常用功能。

笔者的nginx代理配置如下

```nginx
server {
    listen 80; # Nginx 监听的本地端口
    server_name 10.0.7.210; # 绑定的本地 IP
	absolute_redirect off;
	underscores_in_headers on;
    
	location / {
        proxy_pass http://10.201.20.19:31160/; # 转发到的远端地址
		client_max_body_size 2048M;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
		
		# 自定义 HTTP 头部转发
		proxy_set_header Cookie $http_cookie;
		proxy_set_header X-Auth-Token $http_x_auth_token;
		proxy_set_header User_id $http_user_id;
    }
}
```

#### 1.1.6 邮箱配置

邮箱的配置文件在`src/main/resource/application-mail.yml`。

邮箱主要用于对于devops需求和bug审查不合格的人，邮箱通知其整改。

邮箱的最小配置如下：

```yaml
mail:
  host: 10.0.7.210
  port: 587
  userName: yinhuasheng@unicloud.com
  password: MTIzNDU2
```

其中邮箱请使用公司的`unicloud`邮箱，**密码需要进行Base64加密**。

这里可以看到我们的邮箱host又是笔者的ip`10.0.7.210`，因为笔者部署的服务器无法访问通公司邮箱服务器（公司邮箱服务器需要外网访问能力，大部分服务器都无法访问外网），因此笔者本地通过nginx做了个4层代理。

如果你部署的devops-heler服务器也无法访问通公司邮箱服务，那你也起个代理做中转。如果你部署的devops-heler服务器可以访问通笔者自己的`10.0.7.210`地址，那你也可以**直接用笔者的代理，无需自己再配置代理**。将邮箱的配置文件host设为`10.0.7.210`即可。

笔者的nginx代理配置如下：

```nginx
stream {
    upstream mail_server {
        server mail.unicloud.com:587;
    }

    server {
        listen 587;
        proxy_pass mail_server;
    }
}
```

#### 1.1.7 seafile配置

seafile的配置文件在`src/main/resource/application-seafile.yml`。

seafile的主要作用是转测归档和开发转测。

**注1：本组使用seafile来作为转测和测试期间的迭代，devops-helper中大量预设了seafile的一些目录名/文件名规范，如果其他组与本组使用规范不同，则整个转测归档和开发转测功能均不可用。**

注2：考虑到每个组对于seafile的使用方法可能均不同，为便于其他组二次开发，devops-helper中提供了seafile便捷可用的API，如增删改查seafile的文件和目录功能，详见源码`com.coderzoe.component.seafile.service#SeafileService`

本组转测期间对于seafile的转测使用如下：

1. 每个迭代开始会在根目录下创建yyyy年MMdd迭代目录，里面存放本次迭代期间的转测内容
2. 迭代开始会在根目录/yyyy年MMdd迭代目录下创建new_yyyyMMdd目录存放本次迭代初始转测的内容，里面包括本次迭代要转测的服务的yaml，sql，脚本等内容。
3. 测试人员每天早上会取前一天目录下的内容部署
4. 如果转测期间出现bug，开发人员修改完bug后，需重新打包，将最新的yaml上传到根目录/yyyy年MMdd迭代/update_yyyyMMdd目录下

以网络A层为例，730迭代的内容如下：

![image-20240823095013009](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240823095013009.png)

![image-20240823095037966](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240823095037966.png)

![image-20240823095051242](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240823095051242.png)

![image-20240823095059224](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240823095059224.png)

![image-20240823095123574](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240823095123574.png)

![image-20240823095131415](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240823095131415.png)

要想使用基于seafile的转测归档和开发转测功能，则需要遵守如下规范：

1. 每个迭代的目录命名必须是yyyy年MMdd迭代这种命名规范，如本组：

   ![image-20240823094351401](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240823094351401.png)

2. 迭代初始的时候，需要**手动先在根目录下建好yyyy年MMdd迭代的目录，然后在yyyy年MMdd迭代目录下创建new_yyyyMMdd的目录，里面放入本次迭代初始转测的yaml、sql脚本等内容**。

   ![image-20240823095349809](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240823095349809.png)

   ![image-20240823095356920](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240823095356920.png)

3. 测试期间如果某个服务出现bug，开发人员修复后，使用devops-helper的开发转测功能，会打包这个服务并将最新的yaml（更新tag为本次打包的tag）上传到update_yyyyMMdd目录下（如果没有update_yyyyMMdd目录，devops-helper会自动创建）

   ![image-20240823095651390](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240823095651390.png)

   ![image-20240823095658570](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240823095658570.png)

   ![image-20240823095707224](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240823095707224.png)



如果想使用开发转测和转测归档功能且上述seafile使用规范与本组相同，那么我们再看下seafile的配置：

```yaml
seafile:
  user-name: yinhuasheng@unicloud.com
  password: MTIzNDU2
  repository-id: 46850dcf-bc23-4652-a251-fcced38a97a8
  base-path: SDN控制器
  archive-types:
    - YAML
    - SQL
    - SHELL
    - REQUIREMENT
  archive-ignore-paths:
    - name: SLB镜像
      match-type: CONTAINS
    - name: final
      match-type: CONTAINS
  archive-sql-names:
    - name: basic
      match-type: CONTAINS
    - name: driver
      match-type: CONTAINS
```

其中**密码需要进行Base64加密**。

 `repository-id`获取方式如下：

![image-20240823140403611](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240823140403611.png)

`base-path`是迭代的根目录名，以网络A层为例：

![image-20240823140456129](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240823140456129.png)

`archive-types`是用于转测归档内容，归档的内容，目前主要包括YAML、SQL、SHELL和需求这几个，会将本次迭代下的最新的YAML、SQL和SHELL归档，其中需求是从devops上拉下来的，需要seafile迭代目录包含yyyy年MM，devops的迭代目录包含yy年MM或者yyyy年MM。

`archive-ignore-paths`是归档的时候忽略的目录，比如对于网络A层，包含SLB镜像字眼的目录不参与归档，`match-type`是匹配方式，CONTAINS是包含就算，EQUAL是完全相等才算，REGEX是正则匹配。

`archive-sql-names`是要归档的SQL文件名，`match-type`同上。

### 1.2 Docker部署

配置完项目后，我们就可以部署服务了，本服务的Dockerfile如下：

```dockerfile
# 基础镜像
FROM openjdk:21-jdk

# 设置工作目录
WORKDIR /app

# 将应用程序复制到工作目录
COPY target/devops-helper-1.0.jar /app/app.jar

# 设置时区
ENV TZ=Asia/Shanghai


# 暴露容器端口
EXPOSE 8765 8766

# 配置容器启动命令
ENTRYPOINT ["java","-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8766", "-jar", "app.jar"]

```

其中项目启动依赖于openjdk:21镜像，如果所部署的服务器没有镜像jdk21可以去`10.247.146.2`的`/home/yhs/tmp/openjdk-21.tar`获取，然后load进本地的docker。

本文以网络UCA的部署为例：网络UCA的项目部署在`10.247.146.2`的`/home/uca-network/devops-helper`目录下，我们以这个目录为部署根目录。其他组替换为自己组部署的服务器和部署的根目录即可。

在该根目录下，我们需要执行如下指令：

```shell
mkdir log config target db && \
touch db/devops_helper.sqlite && \
chmod -R +x *
```

1. 将我们项目的Dockerfile拷贝到刚才建的根目录`/home/uca-network/devops-helper`下（**以本组根目录为准**，每个组要保证目录不同）。

2. 将我们项目的`/target/devops-helper-1.0.jar` 拷贝到根目录的`/target`目录下。
3. 将我们项目的`/src/main/resources`下的9个yaml拷贝到根目录的`/config`下。
4. 在根目录再执行`chmod -R +x *`

准备完后，整个根路径信息如下：

![image-20240823153404601](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240823153404601.png)

在根目录执行：

```shell
docker build -f Dockerfile -t uca-network-devops-helper:v1 . && \
docker run -d -p 48765:8765 \
		  -v /home/uca-network/devops-helper/log:/app/log \
           -v /home/uca-network/devops-helper/config:/app/config \
           -v /home/uca-network/devops-helper/db/devops_helper.sqlite:/app/devops_helper.sqlite \
           --name uca-network-devops-helper \
           uca-network-devops-helper:v1
```

其中需要将上述指令包含uca-network的替换为自己的组名，避免相同的Docker镜像名和Docker容器名。将映射根路径 `/home/uca-network/devops-helper/`替换为自己组的服务根路径，将映射端口`48765`替换为服务器上没被占用的端口。

服务启动后，使用服务器`ip+映射端口+/devops-helper/`即可访问。

## 2. 源码

项目后端使用JDK21+maven3.8.8，框架是SpringBoot3.x。前端使用pnpm+vite。框架是Vue3.X

后端目录结构：

```
devops-helper
├─ log    					  # 日志文件
├─ src.main      		       # 源码
│  ├─ java.com.coderzoe		   # 代码
│  │  ├─ common          # 通用包 主要包含一些工具和常量
│  │  ├─ config          # 全局的一些配置
│  │  ├─ controller      # API入口
│  │  ├─ model    	     # 数据层 
│  │  ├─ repository      # 部分Spring-jpa的数据库层
│  │  ├─ service         # 部分service层
│  │  ├─ component       # 三方服务组件
│  │  │  ├─ devops       # Devops服务
│  │  │  ├─ gitlab       # Gitlab服务
│  │  │  ├─ jenkins      # jenkins服务
│  │  │  ├─ k8s          # k8s服务
│  │  │  ├─ mail         # 邮箱服务
│  │  │  ├─ seafile      # seafile服务
│  ├─ resources		      # 配置文件
│  │  ├─ prpxy		      # 服务代理的模板
│  │  ├─ sql		      # 项目启动的时候初始化执行的sql文件
│  │  ├─ static		      # 前端打完包后放入后端的目录
│  │  ├─ application*.yml  # 各个配置文件
├─ devops-helper.sqlite   	    # 本地数据库
├─ Dockerfile   			   # Dockerfile
├─ pom.xml    				   # maven依赖
└─  README.md    			   # 本文档
```

由于笔者不太会前端，所以没形成前端工程化能力，就不列举前端目录了，只简单说两点：

1. 项目依赖安装：pnpm install、项目启动 pnpm dev、项目打包 pnpm build
2. 如果想前后端单独分开部署或启动调试，前端`/src/config/config.ts`下将`baseUrl`改为后端的ip+端口。如果前端放在后端内一起部署，则`baseUrl`设为空字符串""。
