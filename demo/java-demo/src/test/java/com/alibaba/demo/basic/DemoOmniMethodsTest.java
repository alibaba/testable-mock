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
        Pod pod = preparePod();

        // 使用成员名快速读取成员变量
        List<String> commands = OmniAccessor.get(pod, "command");
        assertEquals(12, commands.size());
        assertEquals("container-cmd-1", commands.get(0));
        assertEquals("liveness-cmd-1", commands.get(3));
        assertEquals("readness-cmd-1", commands.get(6));
        assertEquals("startup-cmd-1", commands.get(9));

        // 使用成员类型快速读取成员变量
        List<Probe> probes = OmniAccessor.get(pod, "{Probe}");
        assertEquals(9, probes.size());
        assertEquals("liveness-cmd-1", probes.get(0).getExec().getCommand());
        assertEquals("readness-cmd-1", probes.get(3).getExec().getCommand());
        assertEquals("startup-cmd-1", probes.get(6).getExec().getCommand());

        // 使用模糊路径快速读取成员变量
        List<String> startupCommands = OmniAccessor.get(pod, "startupProbe/*/command");
        assertEquals(3, startupCommands.size());
        assertEquals("startup-cmd-1", startupCommands.get(0));
        assertEquals("startup-cmd-2", startupCommands.get(1));
        assertEquals("startup-cmd-3", startupCommands.get(2));

        // 使用带下标的模糊路径读取成员变量
        List<String> firstStartupCommands = OmniAccessor.get(pod, "containers[0]/livenessProbe/*/command");
        assertEquals(1, firstStartupCommands.size());
        assertEquals("liveness-cmd-1", firstStartupCommands.get(0));
    }

    @Test
    void should_set_any_member() {

    }

    private Pod preparePod() {
        Pod pod = OmniConstructor.newInstance(Pod.class);
        pod.getSpec().setContainers( new Container[]{ OmniConstructor.newInstance(Container.class),
            OmniConstructor.newInstance(Container.class), OmniConstructor.newInstance(Container.class) } );
        for (int i = 0; i < 3; i++) {
            pod.getSpec().getContainers()[i].setCommand("container-cmd-" + (i + 1));
            pod.getSpec().getContainers()[i].getLivenessProbe().getExec().setCommand("liveness-cmd-" + (i + 1));
            pod.getSpec().getContainers()[i].getReadinessProbe().getExec().setCommand("readness-cmd-" + (i + 1));
            pod.getSpec().getContainers()[i].getStartupProbe().getExec().setCommand("startup-cmd-" + (i + 1));
        }
        return pod;
    }

}
