
package org.springframework.core.type.classreading;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.asm.ClassReader;
import org.springframework.core.NestedIOException;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.lang.Nullable;

/**
 * 简单的元数据读取器
 */
final class SimpleMetadataReader implements MetadataReader {

	//类文件的资源引用
	private final Resource resource;

	//元数据的运行时类Class对象
	private final ClassMetadata classMetadata;

	//注解元数据
	private final AnnotationMetadata annotationMetadata;


	SimpleMetadataReader(Resource resource, @Nullable ClassLoader classLoader) throws IOException {
		//读取类文件
		InputStream is = new BufferedInputStream(resource.getInputStream());
		ClassReader classReader;
		try {
			//通过resource的输入流，来进行Class字节码的读取
			classReader = new ClassReader(is);
		}
		catch (IllegalArgumentException ex) {
			throw new NestedIOException("ASM ClassReader failed to parse class file - " +
					"probably due to a new Java class file version that isn't supported yet: " + resource, ex);
		}
		finally {
			is.close();
		}

		AnnotationMetadataReadingVisitor visitor = new AnnotationMetadataReadingVisitor(classLoader);
		classReader.accept(visitor, ClassReader.SKIP_DEBUG);

		this.annotationMetadata = visitor;
		// (since AnnotationMetadataReadingVisitor extends ClassMetadataReadingVisitor)
		this.classMetadata = visitor;
		this.resource = resource;
	}


	@Override
	public Resource getResource() {
		return this.resource;
	}

	@Override
	public ClassMetadata getClassMetadata() {
		return this.classMetadata;
	}

	@Override
	public AnnotationMetadata getAnnotationMetadata() {
		return this.annotationMetadata;
	}

}
