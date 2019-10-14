
package org.springframework.core.type.classreading;

import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;

/**
 * 用于访问类元数据的简单外观，由ASM {@link org.springframework.asm.ClassReader}读取。
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
public interface MetadataReader {

	//返回类文件的资源引用。
	Resource getResource();

	//读取基础类的基本类元数据。
	ClassMetadata getClassMetadata();

	//读取底层类的完整注释元数据，包括带注释的方法的元数据。
	AnnotationMetadata getAnnotationMetadata();

}
