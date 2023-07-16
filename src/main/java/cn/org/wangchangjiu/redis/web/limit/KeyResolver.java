package cn.org.wangchangjiu.redis.web.limit;

import jakarta.servlet.http.HttpServletRequest;

public interface KeyResolver {

    /**
     *  获取 Key
     * @param request
     * @return
     */
    String resolve(HttpServletRequest request);

    class DefaultKeyResolver implements KeyResolver{

        private static final String EMPTY_KEY = "____EMPTY_KEY__";

        public DefaultKeyResolver() {
        }

        @Override
        public String resolve(HttpServletRequest request) {
            return EMPTY_KEY;
        }
    }
}
