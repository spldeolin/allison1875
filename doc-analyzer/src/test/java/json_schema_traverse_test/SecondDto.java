package json_schema_traverse_test;

import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 一个简单的Dto
 *
 * @author Deolin 2020-07-06
 */
public class SecondDto {

    @NotNull
    @Min(0)
    private Integer age;

    @NotNull
    private BigDecimal[][][][] salary;

    private ThirdDto aaaa;

    public SecondDto() {
    }

    public @NotNull @Min(0) Integer getAge() {
        return this.age;
    }

    public @NotNull BigDecimal[][][][] getSalary() {
        return this.salary;
    }

    public ThirdDto getAaaa() {
        return this.aaaa;
    }

    public void setAge(@NotNull @Min(0) Integer age) {
        this.age = age;
    }

    public void setSalary(@NotNull BigDecimal[][][][] salary) {
        this.salary = salary;
    }

    public void setAaaa(ThirdDto aaaa) {
        this.aaaa = aaaa;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SecondDto)) {
            return false;
        }
        final SecondDto other = (SecondDto) o;
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$age = this.getAge();
        final Object other$age = other.getAge();
        if (this$age == null ? other$age != null : !this$age.equals(other$age)) {
            return false;
        }
        if (!java.util.Arrays.deepEquals(this.getSalary(), other.getSalary())) {
            return false;
        }
        final Object this$aaaa = this.getAaaa();
        final Object other$aaaa = other.getAaaa();
        return this$aaaa == null ? other$aaaa == null : this$aaaa.equals(other$aaaa);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof SecondDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $age = this.getAge();
        result = result * PRIME + ($age == null ? 43 : $age.hashCode());
        result = result * PRIME + java.util.Arrays.deepHashCode(this.getSalary());
        final Object $aaaa = this.getAaaa();
        result = result * PRIME + ($aaaa == null ? 43 : $aaaa.hashCode());
        return result;
    }

    public String toString() {
        return "SecondDto(age=" + this.getAge() + ", salary=" + java.util.Arrays.deepToString(this.getSalary())
                + ", aaaa=" + this.getAaaa() + ")";
    }

}