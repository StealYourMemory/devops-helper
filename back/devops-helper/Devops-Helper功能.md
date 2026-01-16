# Devops-Helper功能

> 鉴于现在devops流程有些麻烦，且理论上可以做到自动化，因此笔者做了个小工具，将基本用到的devops流程做到了半自动化。原理比较简单，就是联动常用的服务，如jenkins，gitlab，devops，seafile，k8s服务器等。

## 打包部署

### 1. 通用部署

#### 1) 打包+部署

![image-20240520111156287](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240520111156287.png)

选择要打包的项目和分支，选择要部署的环境，Devops Helper会自动帮忙生成tag，生成的规则是：非release分支，tag为`dh-yyyyMMdd-v{index}`，如`dh-20240520-v5`；release分支，tag为`yyyyMMdd-v{index}`，如`20240520-v5`。自动生成的版本index是读取的jenkins打包记录，按上述规则在已有的打包记录index上+1。但Tag设置为了输入框，也即你可以不用生成的tag，而自己输入。

考虑到打包和jenkins联动比较强，因此当你一旦选择了项目名后，会自动带出当前项目在jenkins下的打包记录，如下图右侧所示：

![image-20240520111848681](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240520111848681.png)

展示信息主要为打包是否成功，jenkins下打包id，打包的分支，打包的tag，打包耗时时间。点击详情按钮，会展示打包的详细信息：

![image-20240520111952431](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240520111952431.png)

![image-20240520112016242](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240520112016242.png)

这一信息主要是方便开发人员自己手动输入tag的时候，可以查看下已有的tag，避免冲突，且原版的jenkins对于打包记录展示不够直观，如不直接展示分支和tag信息，还得单独点一下，这里做了些优化。

选择想部署的环境，点击打包部署按钮，就会自动调jenkins接口来打包，打包完成后会自动去所在环境的k8s服务器上更新deployment信息，将tag更新为打包的tag，实现部署，且一旦点击打包部署，会实时展示打包和部署详情：

![image-20240520143853421](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240520143853421.png)

![image-20240520143927967](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240520143927967.png)





注：**由于部署的时候，无法很好的判断部署是否成功，所以偷了个懒，只会每隔几秒执行下`kubectl get pod | grep xx` 将执行结果返回给前端，持续3min，使用者自己判断有没有部署成功就好了。**

#### 2) 仅打包

与上面的打包+部署相似，不过这里只打包，不部署。

#### 3) 仅部署

与上面的打包+部署相似，不过这里只部署，不打包，选择要部署的环境和选择打包过的tag，去相应环境部署相应的tag。

这里在下拉tag的时候，格式为tag/分支的形式，方便看这个tag是从哪个分支打包出来的，避免部署错误。

![image-20240520112635150](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240520112635150.png)

### 2. 开发转测

适用场景是：转测期间，开发修复完bug，将改的bug合入release后，打包release分支，然后更新seafile文件。因此这里的主要功能是打包+更新最新的yaml到seafile下。

![image-20240520112914559](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240520112914559.png)

页面的右侧是seafile目录树，点开某一天信息，如

![image-20240520112947252](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240520112947252.png)

点击某个文件，可以查看相应文件：

![image-20240520113018191](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240520113018191.png)

这里的好处是：seafile下部分文件无法查看，且不具有高亮，这里做了优化，支持查看，支持语法高亮。

且将seafile目录结构改为目录树，而非原版的一层一层，避免了来回跳目录的繁琐。

与上面的通用打包类似，选择项目名+打包的分支，会自动带出tag，tag规则与上面的通用打包也一样，输入本次的更新说明，如`[SKZ-1242] 【vlan模型】创建slb、hslb的监听器失败，报错空指针`。

点击打包更新，会自动调jenkins接口进行打包，打包成功后，会自动生成最新yaml文件到seafile，并自动更新《更新说明.txt》文件。

**这里有需要强调的点：**

1. yaml的生成逻辑是在当前迭代目录下以当前项目的上一个yaml作为模板，只更新tag信息。如20240531迭代下的slb yaml如下：

      ![](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240520113613867.png)

      此时最新的yaml为`20240531迭代 -> update_20240520 -> yaml -> uca-network-slb-v4.yaml`

      如果再打包转测，则会以上述yaml作为最新模板。（简单解释为：当前迭代下，日期最靠后的里面版本号最大的那个文件为最新文件模板）

      如果想使用本功能，**一个迭代内必须要有至少一个本项目的yaml为模板才可以(new目录下初始的yaml也可以)**。

2. 生成的yaml会放到update_yyMMdd文件夹下的yaml文件夹内，如今天是20240520，则生成的yaml会放入update_20240520文件夹下的yaml内，**如果不存在这个文件夹，devops-helper会自动帮你建**。

3. 提交的更新说明内容会**尾追加**到update_yyMMdd文件夹的《更新说明.txt》文件内，**如果不存在则会新建一个《更新说明.txt》**

4. 为了找到当前迭代，**会以当前日期的年月作为查找 /SDN控制器下的所有目录**，如今天是20240520，则会查找包含2024年05内容的目录作为本次迭代，**因此迭代名称必须是yyyy年MM这种格式**，如2024年0531迭代

5. 由于上述找yaml模板会按迭代内的文件夹排序，找最新的，**所以每个迭代内的文件夹必须是xx_yyyyMMdd这种格式** 如update_20240524，或new_20240517（如果不是，则这种文件夹会被忽略）。

6. 与上面一样，为找到最新模板，更新yaml的命名必须是jenkins下的项目名-yyyymmdd-v%d.yaml，或对于初始yaml 不带v-%d 如 uca-network-slb.yaml 或 uca-network-core-basic-20240523-v2.yaml。这样对于一个日期下某个项目多个版本，可以确定哪个是最新版。

虽然上述看起来限制比较多，但我看到目前seafile基本都是这个规则，且如果你使用Devops Helper会自动按照上述规则创建目录和文件，无需过多担心。

**注：目前seafile暂时不支持传sql**


## 转测审查

主要联动devops，适用场景是转测或者转测前检查devops下是否有不符合规范的需求/bug，如果有可以一键发送邮件通知，让大家自己修改不合格内容，避免质检那边查出我们的较多问题。

对于需求，不符合规范的标准是：

1. 状态不是未完成
2. 史诗下挂了非特性
3. 特性下挂了非用户故事
4. 史诗下没挂特性
5. 特性下没挂用户故事
6. 特性下挂的用户故事 < 5
7. 用户故事下挂了非任务

对于Bug，不符合规范的标准是：

1. 状态不是已完成

**考虑到devops比较卡，接口请求比较费时，所以在做的时候，按迭代来筛选，只会审查某个迭代下的需求和Bug**，所以需要先选择迭代版本。

选择迭代版本：

![image-20240520115254346](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240520115254346.png)

![image-20240520115311178](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240520115311178.png)

其中，如果认为审查结果没问题，审查结果可以删除

![image-20240520115342400](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240520115342400.png)

如果一个需求下审查结果都删除了，则代表审查成功，发邮件的时候不会发成功的审查

![image-20240520115432194](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240520115432194.png)

这主要是为了方便自定义。

**注：对于devops需求下没有责任人的，会默认选择需求的父需求的责任人，如果父需求也没有，则会继续往上找。这主要是为了责任人筛选和邮件通知功能。**

另外，邮箱的发送是根据用户名匹配的，根据用户名查找用户的邮箱也是调用的devops的接口：

![](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240913093732327.png)

项目会在启动的时候查询这个接口，将用户名与邮箱映射关系缓存起来，等到需要邮件发送的时候再取出用。

## 转测归档

目前本组Devops的相关服务迭代主要在seafile上，当一次迭代完成后，往往需要归档本次迭代的内容。所谓归档就是将本次迭代最终的服务yaml、shell、sql等整体出来。

转测归档的逻辑如下：选择要归档的迭代版本，点击生成归档

![image-20240724162720444](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240724162720444.png)

这时就会在本地迭代的目录下创建一个dh-final目录，将本次迭代的归档内容放入这个目录。

![image-20240724162857791](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240724162857791.png)



目前迭代归档的生成依赖于一些条件：

2. 归档内容会放入final-dh目录，如果不存在会新建，**如果存在final-dh会删除之前的内容，所以如果原final-dh存在重要文件，请先备份！！！**
3. 归档的内容**不会查询SLB镜像、HSLB镜像、以及包含目录名包含final的内容**，所以归档的shell不包含这些目录下的内容
4. 合入需求查的是devops的需求列表，需要seafile迭代目录包含yyyy年MM，devops的迭代目录包含yy年MM或者yyyy年MM

## 环境管理

我们的服务要部署到某个环境的k8s下，项目早期的时候，环境是写死的为：老预发布、新预发布和城轨三个环境，后来又加进来了多出口环境。但这些环境以及环境下的k8s服务器都是笔者在后端写死的，考虑到后续可能会有新的环境不断添加，或者已有环境k8s服务器ip可能会更改等情况，于是后续加了环境管理功能。

允许对环境和环境下的k8s服务器进行 查询、添加、删除功能。

![image-20240724163417275](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240724163417275.png)

环境一旦被创建，就可用于打包部署下的部署环境。

这里需要注意的是：

![image-20240724163547749](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240724163547749.png)

环境标识最好使用纯英文，不要有中文和特殊字符。

额外配置是针对环境下如何部署服务的，不同环境其实部署我们的k8s服务是不同的，部署时采取的策略也会不同。

其中：

* REBIRTH_V1 是指rebirth1.0，如新预发布环境，直接更新yaml镜像的tag就会自动拉取镜像并更新服务
* NEED_DOCKER_PULL 是指需要使用`docker pull`先拉下来镜像，再更新yaml的tag实现更新服务，主要适配A层城轨环境
* REBIRTH_V2 是指rebirth2.0的环境，这种环境没有docker指令只有nerdctl，同时无法直接拉取我们镜像仓库中打包过的镜像，需要的是先从某个可拉取镜像的环境将镜像`pull`下来，并`save`成文件然后传到目的环境的服务器，在目的环境服务器上`load`传过来的镜像，并`push`镜像到自己的镜像仓库，此时再更新yaml 镜像tag实现服务的更新。其中目前镜像文件传递使用的是scp。



## 服务代理

服务代理使用k8s的service做代理，主要使用场景是k8s服务器可以访问通的服务，但本地办公网访问不通。
如我们在多出口开发的时候，创建的虚墙，k8s服务器可以连上，但本地无法连接，有时本地调试比部署到服务器方便些，就可以将这个虚墙代理出来
源IP和源端口是为了要代理服务的IP和端口，如虚墙IP+830端口，代理服务器是某个环境的k8s服务器，代理端口是代理出的端口。
一旦创建代理成功，就可以使用**代理服务器IP+代理端口**来访问要代理的服务
**注：请保证代理端口在代理服务器上未被占用，如果代理不通，请确保代理服务器与源服务之间是通的**
**注：用完的代理要及时删除！！！避免端口长期占用**

![image-20240724164631231](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240724164631231.png)

创建一个服务代理的信息如下：

![image-20240724164622571](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240724164622571.png)

服务代理的实现比较简单，就是根据提交的这些信息，去k8s服务器下生成一个endPoint的yaml和一个service的yaml，然后`kubectl apply -f`两个yaml即可。 如：

![image-20240724164808565](https://coderzoework.oss-cn-beijing.aliyuncs.com/image-20240724164808565.png)

如上代理的请求会创建：

```yaml
kind: Endpoints
apiVersion: v1
metadata:
  name: p8da49c0ca0e446a486b901d60dc207cf
subsets:
  - addresses:
      - ip: 10.246.150.9
    ports:
      - port: 830

```

```yaml
kind: Service
apiVersion: v1
metadata:
  name: p8da49c0ca0e446a486b901d60dc207cf
spec:
  type: NodePort
  ports:
    - protocol: TCP
      port: 830
      targetPort: 830
      nodePort: 42456
```

如上两个yaml。

同理，代理的删除，也是`kubectl delete -f`如上两个yaml。

一些注意事项：

服务代理用的是k8s service，其底层是ipvs，但如果在代理服务器上直接使用`ipvsadm`操作是不行的，如：

```shell
sudo ipvsadm -A -t 10.246.150.9:830 -s rr
sudo ipvsadm -a -t 10.246.150.9:830 -r 0.0.0.0:42456 -m
```

上述添加的代理记录只会存活几秒，几秒后就会被干掉。这是因为我们k8s服务器上的ipvs是被k8s管理的，k8s会轮询比对当前ipvs的记录与自己的sevice信息，干掉service里没有的记录。这也是为什么我们选择用k8s yaml来实现的原因。

## 审计日志

主要是记录了操作日志，方便一些追溯。由于本项目为了简洁，数据库使用的是sqlite，**受限于sqlite性能，因此日志筛选为尽量走索引，只支持最左原则，也即只支持右侧模糊匹配**。

## 一些补充

### 1. 项目相关

项目在创建初始的时候，就想着简洁，因此本项目无登录，也无权限。但我们联动的这些工具又需要登录，目前使用的登录信息为：

gitlab：用的笔者自己的账号和密码

jenkins：用的admin账号和密码

devops：用的笔者自己的账号和密码

seafile：用的笔者自己的账号和密码

邮箱通知发送者：用的笔者自己的账号和密码

如需变动，请直接联系我修改。

由于本人不是前端开发，之前也基本没有前端开发经验，最初是觉得流程有些麻烦，想着能不能简化，所以这个月现学的前端，一边学一边写的，页面布局比较简陋。且由于没有前端工程化的思维，对组件的抽离做的比较差，存在较多重复代码，所以前端代码的可维护性会比较差，望见谅。

整个项目还在开发和测试阶段，遇到bug或者想做的需求直接拿联系本人，我会在工作允许下的空闲时间做修改迭代。

### 2. 技术栈

前端：pnpm+vue3+typescript+element-plus+vue-router+pinia+axios。

后端：jdk21+springBoot3.x+sqlite+jps+apache mina sshd

一些补充：

本项目初始的时候是使用Java8+SpringBoot2.x，所以你会看到后端代码并无过多关于JDK21的语法，除了项目后期改造使用了虚拟线程。

### 3. 其他组是否可用？

个项目初始的时候，是由于笔者偷懒，不想每次替换版本的时候都去打包和去对应的服务器上更新yaml，加上有些环境更新版本很麻烦，不好拉镜像，比如rebirth2.0的环境。所以在一开始做的时候，主要是考虑到了本组的项目和需求。

如果其他组也想用本服务，我的建议是每个组单独部署本服务，大家不合在一起用，这主要是因为：

1. 上面很多联动服务如gitlab，devops都需要账号登录，目前所用的都是笔者的账号，笔者账号权限有限，只能看到本组的相关内容。
2. 本项目在设计初衷是没有登录和权限的，并且暂时也不打算加权限功能，所以如果使用的人过多，会导致一些互相影响。如环境管理功能，万一有人误删除了别的组加的环境，会带来一些麻烦。
3. 每个组的devops功能其实都有一些小差异，如本组的devops迭代其实依赖于seafile和seafile下的一些默认文件规则，这个功能其实其他组都无法使用，所以其他组可以根据本项目源码，做各组的定制开发，不合在一起更好。
4. 本项目的很多内容都是基于下拉和选择的，如打包部署的时候，是选择一个项目，如slb项目。如果所有组都使用一份服务，会导致下拉列表过长，不利于简单快速的部署服务。

那如果每个组单独部署服务，是否在源代码上有需要更改的内容呢？

是的，因为就像前面所说，这个项目在一开始做的时候，主要是考虑到了本组的项目和需求。但好在笔者在一开始做的时候，也将对本组的强依赖内容都做成了配置文件，如果其他组也需要部署，那理论上只需要更改相应的配置文件即可，基本无需修改项目源码，这对于一些不熟悉Java的开发人员也可以实现本组服务的部署。

对于这些配置项内容和所需做出的修改，可以详见Devops-Helper的部署文档。
