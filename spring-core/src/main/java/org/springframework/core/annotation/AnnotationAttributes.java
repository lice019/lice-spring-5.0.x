/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link LinkedHashMap} subclass representing annotation attribute
 * <em>key-value</em> pairs as read by {@link AnnotationUtils},
 * {@link AnnotatedElementUtils}, and Spring's reflection- and ASM-based
 * {@link org.springframework.core.type.AnnotationMetadata} implementations.
 *
 * <p>Provides 'pseudo-reification' to avoid noisy Map generics in the calling
 * code as well as convenience methods for looking up annotation attributes
 * in a type-safe fashion.
 *
 * @author Chris Beams
 * @author Sam Brannen
 * @author Juergen Hoeller
 * @since 3.1.1
 * @see AnnotationUtils#getAnnotationAttributes
 * @see AnnotatedElementUtils
 */

/**
 * AnnotationAttributes：用于存储注解的属性
 */
@SuppressWarnings("serial")
public class AnnotationAttributes extends LinkedHashMap<String, Object> {

	private static final String UNKNOWN = "unknown";

	//注解的数据类型
	@Nullable
	private final Class<? extends Annotation> annotationType;

	final String displayName;

	boolean validated = false;



	public AnnotationAttributes() {
		this.annotationType = null;
		this.displayName = UNKNOWN;
	}


	public AnnotationAttributes(int initialCapacity) {
		super(initialCapacity);
		this.annotationType = null;
		this.displayName = UNKNOWN;
	}


	public AnnotationAttributes(Map<String, Object> map) {
		super(map);
		this.annotationType = null;
		this.displayName = UNKNOWN;
	}


	public AnnotationAttributes(AnnotationAttributes other) {
		super(other);
		this.annotationType = other.annotationType;
		this.displayName = other.displayName;
		this.validated = other.validated;
	}


	public AnnotationAttributes(Class<? extends Annotation> annotationType) {
		Assert.notNull(annotationType, "'annotationType' must not be null");
		this.annotationType = annotationType;
		this.displayName = annotationType.getName();
	}


	public AnnotationAttributes(String annotationType, @Nullable ClassLoader classLoader) {
		Assert.notNull(annotationType, "'annotationType' must not be null");
		this.annotationType = getAnnotationType(annotationType, classLoader);
		this.displayName = annotationType;
	}

	@SuppressWarnings("unchecked")
	@Nullable
	private static Class<? extends Annotation> getAnnotationType(String annotationType, @Nullable ClassLoader classLoader) {
		if (classLoader != null) {
			try {
				return (Class<? extends Annotation>) classLoader.loadClass(annotationType);
			}
			catch (ClassNotFoundException ex) {
				// Annotation Class not resolvable
			}
		}
		return null;
	}



	@Nullable
	public Class<? extends Annotation> annotationType() {
		return this.annotationType;
	}


	public String getString(String attributeName) {
		return getRequiredAttribute(attributeName, String.class);
	}


	public String[] getStringArray(String attributeName) {
		return getRequiredAttribute(attributeName, String[].class);
	}


	public boolean getBoolean(String attributeName) {
		return getRequiredAttribute(attributeName, Boolean.class);
	}


	@SuppressWarnings("unchecked")
	public <N extends Number> N getNumber(String attributeName) {
		return (N) getRequiredAttribute(attributeName, Number.class);
	}


	@SuppressWarnings("unchecked")
	public <E extends Enum<?>> E getEnum(String attributeName) {
		return (E) getRequiredAttribute(attributeName, Enum.class);
	}


	@SuppressWarnings("unchecked")
	public <T> Class<? extends T> getClass(String attributeName) {
		return getRequiredAttribute(attributeName, Class.class);
	}


	public Class<?>[] getClassArray(String attributeName) {
		return getRequiredAttribute(attributeName, Class[].class);
	}


	public AnnotationAttributes getAnnotation(String attributeName) {
		return getRequiredAttribute(attributeName, AnnotationAttributes.class);
	}


	public <A extends Annotation> A getAnnotation(String attributeName, Class<A> annotationType) {
		return getRequiredAttribute(attributeName, annotationType);
	}


	public AnnotationAttributes[] getAnnotationArray(String attributeName) {
		return getRequiredAttribute(attributeName, AnnotationAttributes[].class);
	}


	@SuppressWarnings("unchecked")
	public <A extends Annotation> A[] getAnnotationArray(String attributeName, Class<A> annotationType) {
		Object array = Array.newInstance(annotationType, 0);
		return (A[]) getRequiredAttribute(attributeName, array.getClass());
	}


	@SuppressWarnings("unchecked")
	private <T> T getRequiredAttribute(String attributeName, Class<T> expectedType) {
		Assert.hasText(attributeName, "'attributeName' must not be null or empty");
		Object value = get(attributeName);
		assertAttributePresence(attributeName, value);
		assertNotException(attributeName, value);
		if (!expectedType.isInstance(value) && expectedType.isArray() &&
				expectedType.getComponentType().isInstance(value)) {
			Object array = Array.newInstance(expectedType.getComponentType(), 1);
			Array.set(array, 0, value);
			value = array;
		}
		assertAttributeType(attributeName, value, expectedType);
		return (T) value;
	}

	private void assertAttributePresence(String attributeName, Object attributeValue) {
		Assert.notNull(attributeValue, () -> String.format(
				"Attribute '%s' not found in attributes for annotation [%s]",
				attributeName, this.displayName));
	}

	private void assertNotException(String attributeName, Object attributeValue) {
		if (attributeValue instanceof Exception) {
			throw new IllegalArgumentException(String.format(
					"Attribute '%s' for annotation [%s] was not resolvable due to exception [%s]",
					attributeName, this.displayName, attributeValue), (Exception) attributeValue);
		}
	}

	private void assertAttributeType(String attributeName, Object attributeValue, Class<?> expectedType) {
		if (!expectedType.isInstance(attributeValue)) {
			throw new IllegalArgumentException(String.format(
					"Attribute '%s' is of type %s, but %s was expected in attributes for annotation [%s]",
					attributeName, attributeValue.getClass().getSimpleName(), expectedType.getSimpleName(),
					this.displayName));
		}
	}


	@Override
	public Object putIfAbsent(String key, Object value) {
		Object obj = get(key);
		if (obj == null) {
			obj = put(key, value);
		}
		return obj;
	}

	@Override
	public String toString() {
		Iterator<Map.Entry<String, Object>> entries = entrySet().iterator();
		StringBuilder sb = new StringBuilder("{");
		while (entries.hasNext()) {
			Map.Entry<String, Object> entry = entries.next();
			sb.append(entry.getKey());
			sb.append('=');
			sb.append(valueToString(entry.getValue()));
			sb.append(entries.hasNext() ? ", " : "");
		}
		sb.append("}");
		return sb.toString();
	}

	private String valueToString(Object value) {
		if (value == this) {
			return "(this Map)";
		}
		if (value instanceof Object[]) {
			return "[" + StringUtils.arrayToDelimitedString((Object[]) value, ", ") + "]";
		}
		return String.valueOf(value);
	}



	@Nullable
	public static AnnotationAttributes fromMap(@Nullable Map<String, Object> map) {
		if (map == null) {
			return null;
		}
		if (map instanceof AnnotationAttributes) {
			return (AnnotationAttributes) map;
		}
		return new AnnotationAttributes(map);
	}

}
