package me.maru.anno;

import java.lang.annotation.*;

/**
 * Fake lombok :
 * 1. 클래스 선언부 위로 선언할 시, 클래스 안에있는
 * 필드를 모두 인식하여, 바이트코드에서 getter 메서드를 자동생성
 * 2. @Get 메서드는 추 후 문제가 될 수 있으며, openApi 를 사용하여 개발한 것이
 * 아니라는점을 알고 사용하시길 바랍니다.
 *
 * @author  maru
 * @since 1.0
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Get {
}
