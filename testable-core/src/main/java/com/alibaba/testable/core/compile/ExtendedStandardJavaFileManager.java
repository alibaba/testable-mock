package com.alibaba.testable.core.compile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

/**
 * Created by trung on 5/3/15. Edited by turpid-monkey on 9/25/15, completed
 * support for multiple compile units.
 */
public class ExtendedStandardJavaFileManager extends
		ForwardingJavaFileManager<JavaFileManager> {

	private List<CompiledCode> compiledCode = new ArrayList<CompiledCode>();
	private DynamicClassLoader cl;

	/**
	 * Creates a new instance of ForwardingJavaFileManager.
	 *
	 * @param fileManager
	 *            delegate to this file manager
	 * @param cl
	 */
	protected ExtendedStandardJavaFileManager(JavaFileManager fileManager,
			DynamicClassLoader cl) {
		super(fileManager);
		this.cl = cl;
	}

	@Override
	public JavaFileObject getJavaFileForOutput(
			JavaFileManager.Location location, String className,
			JavaFileObject.Kind kind, FileObject sibling) throws IOException {

		try {
			CompiledCode innerClass = new CompiledCode(className);
			compiledCode.add(innerClass);
			cl.addCode(innerClass);
			return innerClass;
		} catch (Exception e) {
			throw new RuntimeException(
					"Error while creating in-memory output file for "
							+ className, e);
		}
	}

	@Override
	public ClassLoader getClassLoader(JavaFileManager.Location location) {
		return cl;
	}
}
