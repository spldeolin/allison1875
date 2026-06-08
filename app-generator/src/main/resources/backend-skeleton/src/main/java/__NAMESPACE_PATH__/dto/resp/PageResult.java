package __NAMESPACE__.dto.resp;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 分页结果
 */
@Data
public class PageResult<T> {

    /**
     * 总条数
     */
    private long total;

    /**
     * 本页数据
     */
    private List<T> list;

    public static <T> PageResult<T> of(long total, List<T> list) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setList(list);
        return result;
    }

    public static <T> PageResult<T> empty() {
        return of(0, new ArrayList<>());
    }

}
