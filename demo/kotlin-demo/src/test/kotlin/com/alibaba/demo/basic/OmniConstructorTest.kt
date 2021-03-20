package com.alibaba.demo.basic

import com.alibaba.demo.basic.model.omni.Container
import com.alibaba.demo.basic.model.omni.Pod
import com.alibaba.demo.basic.model.omni.PodStatus
import com.alibaba.demo.basic.model.omni.Probe
import com.alibaba.testable.core.tool.OmniAccessor
import com.alibaba.testable.core.tool.OmniConstructor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * 演示快速创建任意对象和使用路径访问成员
 * Demonstrate quick object construction and access members by path
 */
internal class OmniConstructorTest {
    @Test
    fun should_construct_any_class() {
        val pod = OmniConstructor.newInstance(Pod::class.java)
        val container = OmniConstructor.newInstance(Container::class.java)

        // 所有基础类型初始化为默认数值
        Assertions.assertEquals(0L, pod.spec!!.activeDeadlineSeconds)

        // 所有枚举类型初始化为第一个可选值
        Assertions.assertEquals(PodStatus.WAITING, pod.status)

        // 所有数组类型初始化为空数组
        Assertions.assertEquals(0, pod.spec!!.containers.size)

        // 任意深度的子孙成员对象都会被初始化
        Assertions.assertEquals("", container.readinessProbe!!.exec!!.command)
    }

    @Test
    fun should_get_any_member() {
        val pod = OmniConstructor.newInstance(Pod::class.java)
        pod.spec!!.containers = OmniConstructor.newArray(Container::class.java, 3)
        pod.spec!!.containers[0].command = "container-cmd-1st"
        pod.spec!!.containers[0].livenessProbe!!.exec!!.command = "liveness-cmd-1st"
        pod.spec!!.containers[0].readinessProbe!!.exec!!.command = "readness-cmd-1st"
        pod.spec!!.containers[0].startupProbe!!.exec!!.command = "startup-cmd-1st"
        pod.spec!!.containers[1].command = "container-cmd-2nd"
        pod.spec!!.containers[1].livenessProbe!!.exec!!.command = "liveness-cmd-2nd"
        pod.spec!!.containers[1].readinessProbe!!.exec!!.command = "readness-cmd-2nd"
        pod.spec!!.containers[1].startupProbe!!.exec!!.command = "startup-cmd-2nd"
        pod.spec!!.containers[2].command = "container-cmd-3rd"
        pod.spec!!.containers[2].livenessProbe!!.exec!!.command = "liveness-cmd-3rd"
        pod.spec!!.containers[2].readinessProbe!!.exec!!.command = "readness-cmd-3rd"
        pod.spec!!.containers[2].startupProbe!!.exec!!.command = "startup-cmd-3rd"

        // 使用成员名快速读取成员变量
        val commands = OmniAccessor.get<String>(pod, "command")
        Assertions.assertEquals(12, commands.size)
        Assertions.assertEquals("container-cmd-1st", commands[0])
        Assertions.assertEquals("liveness-cmd-1st", commands[3])
        Assertions.assertEquals("readness-cmd-1st", commands[6])
        Assertions.assertEquals("startup-cmd-1st", commands[9])

        // 使用成员类型快速读取成员变量
        val probes: List<Probe> = OmniAccessor.get(pod, "{Probe}")
        Assertions.assertEquals(9, probes.size)
        Assertions.assertEquals("liveness-cmd-1st", probes[0].exec!!.command)
        Assertions.assertEquals("readness-cmd-1st", probes[3].exec!!.command)
        Assertions.assertEquals("startup-cmd-1st", probes[6].exec!!.command)

        // 使用模糊路径快速读取成员变量
        val startupCommands = OmniAccessor.get<String>(pod, "startupProbe/*/command")
        Assertions.assertEquals(3, startupCommands.size)
        Assertions.assertEquals("startup-cmd-1st", startupCommands[0])
        Assertions.assertEquals("startup-cmd-2nd", startupCommands[1])
        Assertions.assertEquals("startup-cmd-3rd", startupCommands[2])

        // 使用带下标的路径读取成员变量
        val firstStartupCommands: List<Probe> = OmniAccessor.get(pod, "containers[0]/livenessProbe")
        Assertions.assertEquals(1, firstStartupCommands.size)
        Assertions.assertEquals("liveness-cmd-1st", firstStartupCommands[0].exec!!.command)
    }

    @Test
    fun should_set_any_member() {
        val pod = OmniConstructor.newInstance(Pod::class.java)
        pod.spec!!.containers = OmniConstructor.newArray(Container::class.java, 3)

        // 使用模糊路径批量给成员变量赋值
        OmniAccessor.set(pod, "containers/command", "container-cmd")
        OmniAccessor.set(pod, "{Probe}/*/command", "probe-cmd")
        Assertions.assertEquals("container-cmd", pod.spec!!.containers[0].command)
        Assertions.assertEquals("probe-cmd", pod.spec!!.containers[1].readinessProbe!!.exec!!.command)
        Assertions.assertEquals("probe-cmd", pod.spec!!.containers[2].livenessProbe!!.exec!!.command)

        // 使用带下标的路径给成员变量赋值
        OmniAccessor.set(pod, "containers[1]/*/*/command", "probe-cmd-2nd")
        Assertions.assertEquals("probe-cmd", pod.spec!!.containers[0].livenessProbe!!.exec!!.command)
        Assertions.assertEquals("probe-cmd-2nd", pod.spec!!.containers[1].livenessProbe!!.exec!!.command)
    }
}
