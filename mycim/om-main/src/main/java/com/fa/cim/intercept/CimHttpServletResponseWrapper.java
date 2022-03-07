package com.fa.cim.intercept;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * description:
 * <p>CimHttpServletResponseWrapper .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2019/7/22         ********             ZQI               create file
 *
 * @author ZQI
 * @date 2019/7/22 10:18
 * @copyright 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class CimHttpServletResponseWrapper extends HttpServletResponseWrapper {

    private ByteArrayOutputStream buffer = null;

    private ServletOutputStream out = null;

    public CimHttpServletResponseWrapper(HttpServletResponse response) {
        super(response);
        buffer = new ByteArrayOutputStream();
        out = new WrapperOutputStream(buffer);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return out;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (out != null) {
            out.flush();
        }
    }

     /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * -------------------------------------------------------------------------------------------------------------------
      * 
      * @return 
      * @author ZQI
      * @date 2019/7/22 10:31:43
     */
    public byte[] getContent() throws IOException {
        this.flushBuffer();
        return buffer.toByteArray();
    }

     /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * -------------------------------------------------------------------------------------------------------------------
      * 
      * @author ZQI
      * @date 2019/7/22 10:22:28
     */
    class WrapperOutputStream extends ServletOutputStream {

        private ByteArrayOutputStream bos = null;

        public WrapperOutputStream(ByteArrayOutputStream bos) {
            this.bos = bos;
        }

        @Override
        public void write(int b) throws IOException {
            bos.write(b);
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setWriteListener(WriteListener arg0) {
        }
    }
}
