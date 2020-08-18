import org.junit.Test;

import static com.indoormap.map.PublicData.setRegionRange;
import static com.indoormap.map.PublicData.getRegionRange;

/**
 * Created by aaron on 16/7/11.
 * 自定义的单元测试类
 */


public class MTest {

    @Test
    public void test1() {
        System.out.println("ioyheiugyu");
    }

    @Test
    public void regionTest(){
        setRegionRange(6);
        int[] region=getRegionRange(16,30);

        if(region !=null) {
            for (int i = 0; i < 4; i++) {
                System.out.print(region[i] + ";");
            }
            System.out.println();
        }
        else{
            System.out.println("无法判断在哪个区域");
        }
    }
}