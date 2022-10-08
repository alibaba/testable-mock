package com.alibaba.testable.core.compile;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;

/**
 * Created by trung on 5/3/15.
 */
public class SourceCode extends SimpleJavaFileObject {
	private final String contents;
	private final String className;

	public SourceCode(String className, String contents) {
		super(URI.create("string:///" + className.replace('.', '/')
				+ Kind.SOURCE.extension), Kind.SOURCE);
		this.contents = contents;
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) {
		return contents;
	}
}
