package __NAMESPACE__.util;

import java.util.List;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;

public class PageUtils {

    private PageUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static <T> PageInfo<T> transferType(List<?> records, List<T> transfered) {
        PageInfo<?> recordPage = new PageInfo<>(records);
        Page<T> page = new Page<>(recordPage.getPageNum(), recordPage.getPageSize());
        page.setTotal(recordPage.getTotal());
        PageInfo<T> result = PageInfo.of(page);
        result.setList(transfered);
        return result;
    }

}
