package com.coderzoe.component.k8s.device;

import com.coderzoe.common.Result;
import com.coderzoe.common.enums.DeviceStatus;
import com.coderzoe.common.exception.CommonException;
import com.coderzoe.component.k8s.model.K8sServerConnectConfig;
import com.coderzoe.component.k8s.model.request.CmdRequest;
import com.coderzoe.component.k8s.model.request.ScpRequest;
import com.coderzoe.component.k8s.model.response.CmdResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionListener;
import org.apache.sshd.scp.client.ScpRemote2RemoteTransferHelper;

import java.io.ByteArrayOutputStream;
import java.util.EnumSet;
import java.util.Objects;

import static com.coderzoe.common.Constants.CLIENT;

/**
 * @author yinhuasheng
 * @date 2024/8/19 16:46
 */
@Slf4j
public class Device {
    private final DeviceMetaData metaData;
    private DeviceStatus status;
    private ClientSession session;
    private final K8sServerConnectConfig connectConfig;

    public Device(DeviceMetaData metaData, K8sServerConnectConfig connectConfig) {
        this.metaData = metaData;
        this.connectConfig = connectConfig;
        this.status = DeviceStatus.INIT;
    }

    public void connect() {
        try {
            if (this.status != DeviceStatus.DEATH) {
                this.doConnect();
            }
        } catch (Exception e) {
            this.status = DeviceStatus.DISCONNECT;
            log.error("与设备{}:{}建立连接失败,失败原因:",
                    metaData.getIp(),
                    metaData.getPort(),
                    e);
        }
    }

    public void doConnect() throws Exception {
        log.info("准备与设备{}:{}建立连接", this.metaData.getIp(), this.metaData.getPort());

        //连接，配置连接超时时间，这里将连接与认证设置成阻塞同步等待，并配置超时时间
        ConnectFuture connectFuture = CLIENT.connect(metaData.getUserName(), metaData.getIp(), metaData.getPort())
                .verify(connectConfig.getConnectTimeout());
        if (connectFuture.isConnected()) {
            this.session = connectFuture.getSession();
        } else {
            this.status = DeviceStatus.DISCONNECT;
            log.error("与设备{}:{}建立连接失败,失败原因:", metaData.getIp(), this.metaData.getPort(), connectFuture.getException());
            throw new CommonException(connectFuture.getException().getMessage());
        }

        this.session.addPasswordIdentity(metaData.getPassword());
        AuthFuture authFuture = this.session.auth().verify(connectConfig.getAuthTimeout());
        if (!authFuture.isSuccess()) {
            log.error("与设备{}:{}建立连接失败,失败原因:认证失败",
                    this.metaData.getIp(),
                    this.metaData.getPort(),
                    authFuture.getException());
            throw new CommonException("认证失败:" + authFuture.getException().getMessage());
        }
        this.status = DeviceStatus.CONNECTING;

        //session中注册监听器，监听断开连接
        this.session.addSessionListener(new SessionListener() {
            @Override
            public void sessionClosed(Session session) {
                log.error("监听到设备{}:{}断开连接",
                        metaData.getIp(),
                        metaData.getPort());
                status = DeviceStatus.DISCONNECT;
            }
        });

        log.info("与设备{}:{}建立连接成功",
                this.metaData.getIp(),
                this.metaData.getPort());
    }

    /**
     * 给设备发送指令
     *
     * @param request 指令信息
     */
    public Result<CmdResponse> send(CmdRequest request) {
        try {
            if (this.session.isClosed() || this.session.isClosing() || this.status == DeviceStatus.DISCONNECT) {
                this.connect();
            }
            if (this.session.isClosed()
                    || this.session.isClosing()
                    || this.status == DeviceStatus.DISCONNECT
                    || this.status == DeviceStatus.DEATH) {
                log.error("设备{}:{}断开连接", this.metaData.getIp(), this.metaData.getPort());
                this.close();
                return Result.fail(this.metaData.getEnvironment() + "设备" + this.metaData.getIp() + ":" + this.metaData.getPort() + "执行指令" + request.getCmd() + "失败,失败原因:设备连接不上");
            }
            try (ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                 ClientChannel channel = session.createExecChannel(request.getCmd())) {
                channel.setOut(responseStream);
                channel.open().verify(this.connectConfig.getWriteTimeout());
                channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), this.connectConfig.getReadTimeout());
                return Result.success(new CmdResponse(responseStream.toString()));
            }
        } catch (Exception e) {
            log.error("设备{}:{}发送失败", this.metaData.getIp(), this.metaData.getPort(), e);
            return Result.fail(this.metaData.getEnvironment() + "设备" + this.metaData.getIp() + ":" + this.metaData.getPort() + "执行指令" + request.getCmd() + "失败,失败原因:" + e.getMessage());
        }
    }

    public Result<CmdResponse> scpSend(ScpRequest request) {
        try {
            ScpRemote2RemoteTransferHelper helper = new ScpRemote2RemoteTransferHelper(request.getSrcDevice().session, request.getDstDevice().session);
            helper.transferFile(request.getSrcFilePath(), request.getDstFilePath(), true);
            return Result.success(null);
        } catch (Exception e) {
            log.error("SCP执行失败，srcDevice{},dstDevice{},srcFile:{},dstFile:{}:", request.getSrcDevice(), request.getDstDevice(), request.getSrcFilePath(), request.getDstFilePath(), e);
            return Result.fail("SCP执行失败，设备" + request.getSrcDevice() + "向" + request.getDstDevice() + "发送scp，源文件" + request.getSrcFilePath() + "目的文件" + request.getDstFilePath() + "失败原因:" + e.getMessage());
        }
    }


    public void close() {
        if (session != null && session.isOpen()) {
            session.close(true);
        }
    }

    public void destroy() {
        log.info("收到设备{}:{}被销毁的请求", this.metaData.getIp(), this.metaData.getPort());
        this.status = DeviceStatus.DEATH;
        this.close();
    }

    /**
     * 重写equals和hash
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Device that = (Device) o;
        return Objects.equals(this.metaData.getIp(), that.metaData.getIp()) && Objects.equals(this.metaData.getPort(), that.metaData.getPort());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.metaData.getIp(), this.metaData.getPort());
    }

    public DeviceMetaData metaData() {
        return this.metaData;
    }

    @Override
    public String toString() {
        return this.metaData.getEnvironment() + "-" + this.metaData.getIp() + ":" + this.metaData.getPort();
    }
}
