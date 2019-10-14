
package org.springframework.web.servlet.handler;

import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;

/**
 * 通过{@link MatchableHandlerMapping}获取请求模式匹配结果的容器，并提供一个方法来进一步从模式中提取URI模板变量。
 *
 * @author Rossen Stoyanchev
 * @since 4.3.1
 */
public class RequestMatchResult {

	//匹配规则
	private final String matchingPattern;

	//需要查找的URL路径
	private final String lookupPath;

	//路径匹配器
	private final PathMatcher pathMatcher;


	/**
	 * Create an instance with a matching pattern.
	 * @param matchingPattern the matching pattern, possibly not the same as the
	 * input pattern, e.g. inputPattern="/foo" and matchingPattern="/foo/".
	 * @param lookupPath the lookup path extracted from the request
	 * @param pathMatcher the PathMatcher used
	 */
	public RequestMatchResult(String matchingPattern, String lookupPath, PathMatcher pathMatcher) {
		Assert.hasText(matchingPattern, "'matchingPattern' is required");
		Assert.hasText(lookupPath, "'lookupPath' is required");
		Assert.notNull(pathMatcher, "'pathMatcher' is required");
		this.matchingPattern = matchingPattern;
		this.lookupPath = lookupPath;
		this.pathMatcher = pathMatcher;
	}


	/**
	 * Extract URI template variables from the matching pattern as defined in
	 * {@link PathMatcher#extractUriTemplateVariables}.
	 * @return a map with URI template variables
	 */
	public Map<String, String> extractUriTemplateVariables() {
		return this.pathMatcher.extractUriTemplateVariables(this.matchingPattern, this.lookupPath);
	}

}
