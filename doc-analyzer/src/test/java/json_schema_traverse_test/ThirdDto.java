package json_schema_traverse_test;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Deolin 2020-08-01
 */
public class ThirdDto {

    private String a;

    @Autowired
    private RootDto[] rootDto;

    public ThirdDto() {
    }

    public String getA() {
        return this.a;
    }

    public RootDto[] getRootDto() {
        return this.rootDto;
    }

    public void setA(String a) {
        this.a = a;
    }

    public void setRootDto(RootDto[] rootDto) {
        this.rootDto = rootDto;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ThirdDto)) {
            return false;
        }
        final ThirdDto other = (ThirdDto) o;
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$a = this.getA();
        final Object other$a = other.getA();
        if (this$a == null ? other$a != null : !this$a.equals(other$a)) {
            return false;
        }
        return java.util.Arrays.deepEquals(this.getRootDto(), other.getRootDto());
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ThirdDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $a = this.getA();
        result = result * PRIME + ($a == null ? 43 : $a.hashCode());
        result = result * PRIME + java.util.Arrays.deepHashCode(this.getRootDto());
        return result;
    }

    public String toString() {
        return "ThirdDto(a=" + this.getA() + ", rootDto=" + java.util.Arrays.deepToString(this.getRootDto()) + ")";
    }

}