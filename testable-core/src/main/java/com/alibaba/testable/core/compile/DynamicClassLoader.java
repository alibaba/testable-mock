package com.alibaba.testable.core.compile;

import java.util.HashMap;
import java.util.Map;

public class DynamicClassLoader extends ClassLoader {

	private Map<String, CompiledCode> customCompiledCode = new HashMap<String, CompiledCode>();

	public DynamicClassLoader(ClassLoader parent) {
		super(parent);
	}

	public void addCode(CompiledCode cc) {
		customCompiledCode.put(cc.getName(), cc);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		CompiledCode cc = customCompiledCode.get(name);
		if (cc == null) {
			return super.findClass(name);
		}
		byte[] byteCode = cc.getByteCode();
		return defineClass(name, byteCode, 0, byteCode.length);
	}
}
