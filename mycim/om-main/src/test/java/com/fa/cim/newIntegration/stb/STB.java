package com.fa.cim.newIntegration.stb;

import com.fa.cim.MycimApplication;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @exception
 * @author Ho
 * @date 2019/8/27 10:52
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
public class STB {
    @Autowired
    private STBCase stbCase;

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void STB_PreparedLot() {
        stbCase.STB_PreparedLot();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void STB_NotPreparedLot() {
        stbCase.STB_NotPreparedLot();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void STB_WaferCount_DifferentWith_ProductCount () {
        stbCase.STB_WaferCount_DifferentWith_ProductCount();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void STB_After_Merging_Wafers_Into_One_Carrier () {
        stbCase.STB_After_Merging_Wafers_Into_One_Carrier();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void STB_After_Merging_Wafers_Into_One_Prepared_Carrier () {
        stbCase.STB_After_Merging_Wafers_Into_One_Prepared_Carrier();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void STB_Into_One_NonSource_Carrier () {
        stbCase.STB_Into_One_NonSource_Carrier();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void STB_One_Carrier_have_25_wafers_to_STB_into_a_same_Carrier_with_5_wafer () {
        stbCase.STB_One_Carrier_have_25_wafers_to_STB_into_a_same_Carrier_with_5_wafer();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void STB_No_carrier_move_generating_multiple_lots () {
        stbCase.STB_No_carrier_move_generating_multiple_lots();
    }

}
