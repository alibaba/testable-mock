package com.alibaba.demo.basic;

import com.alibaba.demo.basic.model.omni.*;
import com.alibaba.testable.core.tool.OmniAccessor;
import com.alibaba.testable.core.tool.OmniConstructor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 演示快速创建任意对象和使用路径访问成员
 * Demonstrate quick object construction and access members by path
 */
class DemoOmniMethodsTest {

    @Test
    void should_construct_any_class() {
        Pod pod = OmniConstructor.newInstance(Pod.class);
        Container container = OmniConstructor.newInstance(Container.class);

        // 所有基础类型初始化为默认数值
        assertEquals(0L, pod.getSpec().getActiveDeadlineSeconds());

        // 所有枚举类型初始化为第一个可选值
        assertEquals(PodStatus.WAITING, pod.getStatus());

        // 所有数组类型初始化为空数组
        assertEquals(0, pod.getSpec().getContainers().length);

        // 任意深度的子孙成员对象都会被初始化
        assertEquals("", container.getReadinessProbe().getExec().getCommand());
    }

    @Test
    void should_get_any_member() {
        Pod pod = OmniConstructor.newInstance(Pod.class);
        pod.getSpec().setContainers( OmniConstructor.newArray(Container.class, 3) );
        pod.getSpec().getContainers()[0].setCommand("container-cmd-1st");
        pod.getSpec().getContainers()[0].getLivenessProbe().getExec().setCommand("liveness-cmd-1st");
        pod.getSpec().getContainers()[0].getReadinessProbe().getExec().setCommand("readness-cmd-1st");
        pod.getSpec().getContainers()[0].getStartupProbe().getExec().setCommand("startup-cmd-1st");
        pod.getSpec().getContainers()[1].setCommand("container-cmd-2nd");
        pod.getSpec().getContainers()[1].getLivenessProbe().getExec().setCommand("liveness-cmd-2nd");
        pod.getSpec().getContainers()[1].getReadinessProbe().getExec().setCommand("readness-cmd-2nd");
        pod.getSpec().getContainers()[1].getStartupProbe().getExec().setCommand("startup-cmd-2nd");
        pod.getSpec().getContainers()[2].setCommand("container-cmd-3rd");
        pod.getSpec().getContainers()[2].getLivenessProbe().getExec().setCommand("liveness-cmd-3rd");
        pod.getSpec().getContainers()[2].getReadinessProbe().getExec().setCommand("readness-cmd-3rd");
        pod.getSpec().getContainers()[2].getStartupProbe().getExec().setCommand("startup-cmd-3rd");

        // 使用成员名快速读取成员变量
        List<String> commands = OmniAccessor.get(pod, "command");
        assertEquals(12, commands.size());
        assertEquals("container-cmd-1st", commands.get(0));
        assertEquals("liveness-cmd-1st", commands.get(3));
        assertEquals("readness-cmd-1st", commands.get(6));
        assertEquals("startup-cmd-1st", commands.get(9));

        // 使用成员类型快速读取成员变量
        List<Probe> probes = OmniAccessor.get(pod, "{Probe}");
        assertEquals(9, probes.size());
        assertEquals("liveness-cmd-1st", probes.get(0).getExec().getCommand());
        assertEquals("readness-cmd-1st", probes.get(3).getExec().getCommand());
        assertEquals("startup-cmd-1st", probes.get(6).getExec().getCommand());

        // 使用模糊路径快速读取成员变量
        List<String> startupCommands = OmniAccessor.get(pod, "startupProbe/*/command");
        assertEquals(3, startupCommands.size());
        assertEquals("startup-cmd-1st", startupCommands.get(0));
        assertEquals("startup-cmd-2nd", startupCommands.get(1));
        assertEquals("startup-cmd-3rd", startupCommands.get(2));

        // 使用带下标的路径读取成员变量
        List<Probe> firstStartupCommands = OmniAccessor.get(pod, "containers[0]/livenessProbe");
        assertEquals(1, firstStartupCommands.size());
        assertEquals("liveness-cmd-1st", firstStartupCommands.get(0).getExec().getCommand());
    }

    @Test
    void should_set_any_member() {
        Pod pod = OmniConstructor.newInstance(Pod.class);
        pod.getSpec().setContainers( OmniConstructor.newArray(Container.class, 3) );

        // 使用模糊路径批量给成员变量赋值
        OmniAccessor.set(pod, "containers/command", "container-cmd");
        OmniAccessor.set(pod, "{Probe}/*/command", "probe-cmd");
        assertEquals("container-cmd", pod.getSpec().getContainers()[0].getCommand());
        assertEquals("probe-cmd", pod.getSpec().getContainers()[1].getReadinessProbe().getExec().getCommand());
        assertEquals("probe-cmd", pod.getSpec().getContainers()[2].getLivenessProbe().getExec().getCommand());

        // 使用带下标的路径给成员变量赋值
        OmniAccessor.set(pod, "containers[1]/*/*/command", "probe-cmd-2nd");
        assertEquals("probe-cmd", pod.getSpec().getContainers()[0].getLivenessProbe().getExec().getCommand());
        assertEquals("probe-cmd-2nd", pod.getSpec().getContainers()[1].getLivenessProbe().getExec().getCommand());
    }

}
