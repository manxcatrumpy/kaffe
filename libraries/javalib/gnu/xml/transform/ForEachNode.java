/*
 * ForEachNode.java
 * Copyright (C) 2004 The Free Software Foundation
 * 
 * This file is part of GNU JAXP, a library.
 *
 * GNU JAXP is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * GNU JAXP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library.  Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent
 * modules, and to copy and distribute the resulting executable under
 * terms of your choice, provided that you also meet, for each linked
 * independent module, the terms and conditions of the license of that
 * module.  An independent module is a module which is not derived from
 * or based on this library.  If you modify this library, you may extend
 * this exception to your version of the library, but you are not
 * obliged to do so.  If you do not wish to do so, delete this
 * exception statement from your version. 
 */

package gnu.xml.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Node;
import gnu.xml.xpath.Expr;

/**
 * A template node representing an XSLT <code>for-each</code> instruction.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
final class ForEachNode
  extends TemplateNode
{

  final Expr select;
  final List sortKeys;

  ForEachNode(TemplateNode children, TemplateNode next, Expr select,
              List sortKeys)
  {
    super(children, next);
    this.select = select;
    this.sortKeys = sortKeys;
  }

  void apply(Stylesheet stylesheet, String mode,
             Node context, int pos, int len,
             Node parent, Node nextSibling)
    throws TransformerException
  {
    if (children != null)
      {
        Object ret = select.evaluate(context, pos, len);
        //System.out.println(toString() + ": " + context+" -> "+ret);
        if (ret instanceof Collection)
          {
            Collection ns = (Collection) ret;
            List list = new ArrayList(ns);
            if (sortKeys != null)
              {
                Collections.sort(list, new XSLComparator(sortKeys));
              }
            else
              {
                Collections.sort(list, documentOrderComparator);
              }
            int l = list.size();
            int p = 1;
            for (Iterator i = list.iterator(); i.hasNext(); )
              {
                Node node = (Node) i.next();
                children.apply(stylesheet, mode,
                               node, p++, l,
                               parent, nextSibling);
              }
          }
      }
    if (next != null)
      {
        next.apply(stylesheet, mode,
                   context, pos, len,
                   parent, nextSibling);
      }
  }

  public String toString()
  {
    StringBuffer buf = new StringBuffer(getClass().getName());
    buf.append('[');
    buf.append("select=");
    buf.append(select);
    buf.append(']');
    return buf.toString();
  }
  
}