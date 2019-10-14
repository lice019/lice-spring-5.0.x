
package org.springframework.web.servlet.mvc.support;

import java.util.Collection;
import java.util.Map;

import org.springframework.lang.Nullable;
import org.springframework.ui.ModelMap;
import org.springframework.validation.DataBinder;

/**
 * RedirectAttributesModelMap；将模型的值保存起来，在页面重定向时，数据不丢失
 * {@link RedirectAttributes}的{@link ModelMap}实现，
 * 它使用{@link DataBinder}将值格式化为字符串。还提供了一个存储flash属性的地方，
 * 这样它们就可以在重定向时存活下来，而不需要嵌入到重定向URL中。
 *
 * @author Rossen Stoyanchev
 * @since 3.1
 */
@SuppressWarnings("serial")
public class RedirectAttributesModelMap extends ModelMap implements RedirectAttributes {

	//数据绑定对象
	@Nullable
	private final DataBinder dataBinder;

	private final ModelMap flashAttributes = new ModelMap();



	public RedirectAttributesModelMap() {
		this(null);
	}


	public RedirectAttributesModelMap(@Nullable DataBinder dataBinder) {
		this.dataBinder = dataBinder;
	}



	@Override
	public Map<String, ?> getFlashAttributes() {
		return this.flashAttributes;
	}


	@Override
	public RedirectAttributesModelMap addAttribute(String attributeName, @Nullable Object attributeValue) {
		super.addAttribute(attributeName, formatValue(attributeValue));
		return this;
	}

	@Nullable
	private String formatValue(@Nullable Object value) {
		if (value == null) {
			return null;
		}
		return (this.dataBinder != null ? this.dataBinder.convertIfNecessary(value, String.class) : value.toString());
	}


	@Override
	public RedirectAttributesModelMap addAttribute(Object attributeValue) {
		super.addAttribute(attributeValue);
		return this;
	}


	@Override
	public RedirectAttributesModelMap addAllAttributes(@Nullable Collection<?> attributeValues) {
		super.addAllAttributes(attributeValues);
		return this;
	}


	@Override
	public RedirectAttributesModelMap addAllAttributes(@Nullable Map<String, ?> attributes) {
		if (attributes != null) {
			attributes.forEach(this::addAttribute);
		}
		return this;
	}


	@Override
	public RedirectAttributesModelMap mergeAttributes(@Nullable Map<String, ?> attributes) {
		if (attributes != null) {
			attributes.forEach((key, attribute) -> {
				if (!containsKey(key)) {
					addAttribute(key, attribute);
				}
			});
		}
		return this;
	}

	@Override
	public Map<String, Object> asMap() {
		return this;
	}


	@Override
	public Object put(String key, @Nullable Object value) {
		return super.put(key, formatValue(value));
	}


	@Override
	public void putAll(@Nullable Map<? extends String, ? extends Object> map) {
		if (map != null) {
			map.forEach((key, value) -> put(key, formatValue(value)));
		}
	}

	@Override
	public RedirectAttributes addFlashAttribute(String attributeName, @Nullable Object attributeValue) {
		this.flashAttributes.addAttribute(attributeName, attributeValue);
		return this;
	}

	@Override
	public RedirectAttributes addFlashAttribute(Object attributeValue) {
		this.flashAttributes.addAttribute(attributeValue);
		return this;
	}

}
