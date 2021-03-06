package org.zstack.kvm;

import org.springframework.beans.factory.annotation.Autowired;
import org.zstack.compute.vm.VmTracer;
import org.zstack.header.errorcode.ErrorCode;
import org.zstack.header.host.*;
import org.zstack.header.rest.JsonAsyncRESTCallback;
import org.zstack.header.rest.RESTFacade;
import org.zstack.header.vm.VmInstanceState;
import org.zstack.kvm.KVMAgentCommands.VmSyncCmd;
import org.zstack.kvm.KVMAgentCommands.VmSyncResponse;
import org.zstack.kvm.KVMConstant.KvmVmState;
import org.zstack.utils.Utils;
import org.zstack.utils.logging.CLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class KvmVmSyncPingTask extends VmTracer implements HostPingTaskExtensionPoint, KVMHostConnectExtensionPoint, HostConnectionReestablishExtensionPoint {
    private static final CLogger logger = Utils.getLogger(KvmVmSyncPingTask.class);
    
    @Autowired
    private RESTFacade restf;
    @Autowired
    private KVMHostFactory factory;

    private void syncVm(final KVMHostContext host) {
        VmSyncCmd cmd = new VmSyncCmd();
        restf.asyncJsonPost(host.buildUrl(KVMConstant.KVM_VM_SYNC_PATH), cmd, new JsonAsyncRESTCallback<VmSyncResponse>() {
            @Override
            public void fail(ErrorCode err) {
                logger.warn(String.format("unable to do vm sync on host[uuid:%s, ip:%s] because %s", host.getInventory().getUuid(), host.getInventory().getManagementIp(), err));
            }

            @Override
            public void success(VmSyncResponse ret) {
                if (ret.isSuccess()) {
                    Map<String, VmInstanceState> states = new HashMap<String, VmInstanceState>(ret.getStates().size());
                    for (Map.Entry<String, String> e : ret.getStates().entrySet()) {
                        VmInstanceState state = KvmVmState.valueOf(e.getValue()).toVmInstanceState();
                        if (state == VmInstanceState.Running || state == VmInstanceState.Unknown) {
                            states.put(e.getKey(), state);
                        }
                    }

                    reportVmState(host.getInventory().getUuid(), states);
                } else {
                    logger.warn(String.format("unable to do vm sync on host[uuid:%s, ip:%s] because %s", host.getInventory().getUuid(), host.getInventory().getManagementIp(), ret.getError()));
                }
            }

            @Override
            public Class<VmSyncResponse> getReturnClass() {
                return VmSyncResponse.class;
            }

        }, TimeUnit.SECONDS, 300);
    }

    @Override
    public void executeTaskAlongWithPingTask(final HostInventory inv) {
        KVMHostContext host = factory.getHostContext(inv.getUuid());
        syncVm(host);

    }

    @Override
    public HypervisorType getHypervisorType() {
        return HypervisorType.valueOf(KVMConstant.KVM_HYPERVISOR_TYPE);
    }

    @Override
    public void kvmHostConnected(KVMHostConnectedContext context) throws KVMHostConnectException {
        syncVm(context);
    }

    @Override
    public void connectionReestablished(HostInventory inv) throws HostException {
        syncVm(factory.getHostContext(inv.getUuid()));
    }

    @Override
    public HypervisorType getHypervisorTypeForReestablishExtensionPoint() {
        return HypervisorType.valueOf(KVMConstant.KVM_HYPERVISOR_TYPE);
    }
}
