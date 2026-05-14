package com.ifx.vm_manager.domain.ports.output;

import com.ifx.vm_manager.domain.model.VirtualMachine;
import com.ifx.vm_manager.domain.model.VmEventType;

public interface VmEventPort {

    void publishVmEvent(VmEventType eventType, VirtualMachine vm);
}
