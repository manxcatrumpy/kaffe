/* EncoderUTF16BE.java -- Encoder for the UTF-16 Big Endian character encoding.
   Copyright (C) 2005 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package gnu.java.io.encode;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

/**
 * This class implements character encoding in the UCS Transformation Format 16
 * Big Endian (UTF-16BE) encoding scheme.
 * 
 * @version 0.0
 * @author Quentin Anciaux (quentin.anciaux@advalvas.be)
 */
public class EncoderUTF16BE extends Encoder
{

  public EncoderUTF16BE(OutputStream out)
  {
    super(out, "UTF16BE");
  }

  /**
   * Returns the number of bytes the specified char array will be encoded
   * into
   */
  public int bytesInCharArray(char[] buf, int offset, int len)
  {
    int num_bytes = 0;
    for (int i = offset; i < offset + len; i++)
      num_bytes += 2;

    return num_bytes;
  }

  /**
   * This method converts a char array to bytes
   */
  public byte[] convertToBytes(char[] buf, int buf_offset, int len,
			       byte[] bbuf, int bbuf_offset)
  {
    int val;
    // Scan the buffer with full validation checks
    for (int i = buf_offset; i < buf_offset + len; i++) {
      byte [] newArray = new BigInteger("" + ((int) buf[i])).toByteArray();
      if (newArray.length < 2)
	{
	  bbuf[bbuf_offset++] = 0;
	  bbuf[bbuf_offset++] = newArray[0];
	}
      else
	{
	  bbuf[bbuf_offset++] = newArray[0];
	  bbuf[bbuf_offset++] = newArray[1];
	}
    }
    return bbuf;
}

  /**
   * Writes a char array as bytes to the underlying stream.
   */
  public void write(char[] buf, int offset, int len) throws IOException
  {
    byte[] bbuf = new byte[bytesInCharArray(buf, offset, len)];
    convertToBytes(buf, offset, len, bbuf, 0);
    out.write(bbuf);
  }

} // class EncoderUTF16BE
