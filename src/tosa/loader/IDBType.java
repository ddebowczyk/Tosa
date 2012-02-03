package tosa.loader;

import gw.lang.reflect.IEnhanceableType;
import tosa.api.IDBTable;
import tosa.api.query.CoreFinder;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/29/10
 * Time: 10:36 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IDBType extends IEnhanceableType {
  IDBTable getTable();
  CoreFinder getFinder();
}
