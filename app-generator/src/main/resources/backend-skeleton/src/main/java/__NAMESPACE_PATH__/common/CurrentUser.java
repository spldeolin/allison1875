package __NAMESPACE__.common;

import __NAMESPACE__.dto.CurrentUserDTO;

/**
 * @author Deolin 2026-06-08
 */
public class CurrentUser {

    private static final ThreadLocal<CurrentUserDTO> USER_CONTEXT = new ThreadLocal<>();

    private CurrentUser() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    /**
     * 设置当前用户
     *
     * @param currentUser 当前用户信息
     */
    public static void set(CurrentUserDTO currentUser) {
        USER_CONTEXT.set(currentUser);
    }

    /**
     * 获取当前用户
     *
     * @return 当前用户信息，如果未设置则返回null
     */
    public static CurrentUserDTO get() {
        return USER_CONTEXT.get();
    }

    /**
     * 获取当前用户名
     *
     * @return 当前用户名，如果未设置则返回null
     */
    public static String getUsername() {
        CurrentUserDTO user = get();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 清除当前用户上下文
     * 应该在请求处理完成后调用，避免线程复用导致的数据污染
     */
    public static void clear() {
        USER_CONTEXT.remove();
    }

}