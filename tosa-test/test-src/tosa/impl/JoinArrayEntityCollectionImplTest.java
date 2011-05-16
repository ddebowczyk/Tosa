package tosa.impl;

import gw.lang.reflect.TypeSystem;
import org.junit.BeforeClass;
import org.junit.Test;
import test.TestEnv;
import tosa.CachedDBObject;
import tosa.api.IDBColumn;
import tosa.api.IDBObject;
import tosa.api.IPreparedStatementParameter;
import tosa.dbmd.DatabaseImpl;
import tosa.loader.DBTypeLoader;
import tosa.loader.IDBType;

import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 5/8/11
 * Time: 3:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class JoinArrayEntityCollectionImplTest {

  @BeforeClass
  static public void resetDB() {
    TestEnv.maybeInit();
    getDB().getDBUpgrader().recreateTables();
  }

  private static DatabaseImpl getDB() {
    DBTypeLoader dbTypeLoader = TypeSystem.getTypeLoader(DBTypeLoader.class);
    return dbTypeLoader.getTypeDataForNamespace("test.testdb");
  }

  private IDBObject createAndCommitFoo() {
    IDBObject bar = new CachedDBObject((IDBType) TypeSystem.getByFullName("test.testdb.Foo"), true);
    update(bar);
    return bar;
  }

  private IDBObject createAndCommitBaz(IDBObject foo) {
    IDBObject baz = createBaz();
    update(baz);
    IDBObject join = createJoin();
    join.setColumnValue("Foo_id", foo.getId());
    join.setColumnValue("Baz_id", baz.getId());
    update(join);
    return baz;
  }

  private IDBObject createBaz() {
    return new CachedDBObject((IDBType) TypeSystem.getByFullName("test.testdb.Baz"), true);
  }

  private IDBObject createJoin() {
    return new CachedDBObject((IDBType) TypeSystem.getByFullName("test.testdb.join_Foo_Baz"), true);
  }

  private void update(IDBObject obj) {
    try {
      obj.update();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private JoinArrayEntityCollectionImpl createList(IDBObject foo) {
    return createList(foo, new QueryExecutorImpl(foo.getDBTable().getDatabase()));
  }

  private JoinArrayEntityCollectionImpl createList(IDBObject foo, QueryExecutor queryExecutor) {
    IDBType bazType = (IDBType) TypeSystem.getByFullName("test.testdb.Baz");
    IDBColumn srcColumn = bazType.getTable().getDatabase().getTable("join_Foo_Baz").getColumn("Foo_id");
    IDBColumn targetColumn = bazType.getTable().getDatabase().getTable("join_Foo_Baz").getColumn("Baz_id");
    return new JoinArrayEntityCollectionImpl(foo, bazType, srcColumn, targetColumn, queryExecutor);
  }

  @Test
  public void testSizeReturnsZeroIfArrayIsEmptyAndHasBeenLoaded() {
    IDBObject foo = createAndCommitFoo();
    JoinArrayEntityCollectionImpl list = createList(foo);
    list.load();
    assertEquals(0, list.size());
  }

  @Test
  public void testSizeReturnsZeroIfArrayIsEmptyAndHasNotBeenLoaded() {
    IDBObject foo = createAndCommitFoo();
    JoinArrayEntityCollectionImpl list = createList(foo);
    assertEquals(0, list.size());
  }

  @Test
  public void testSizeReturnsCorrectSizeForNonEmptyLoadedArray() {
    IDBObject foo = createAndCommitFoo();
    createAndCommitBaz(foo);
    createAndCommitBaz(foo);
    JoinArrayEntityCollectionImpl list = createList(foo);
    list.load();
    assertEquals(2, list.size());
  }

  @Test
  public void testSizeReturnsCorrectSizeForNonEmptyNonLoadedArray() {
    IDBObject foo = createAndCommitFoo();
    createAndCommitBaz(foo);
    createAndCommitBaz(foo);
    JoinArrayEntityCollectionImpl list = createList(foo);
    assertEquals(2, list.size());
  }

//  @Test
//  public void testSizeReturnsOriginalSizeForLoadedArrayAfterChangesInDB() {
//    IDBObject bar = createAndCommitBar();
//    createAndCommitFoo(bar);
//    createAndCommitFoo(bar);
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    list.load();
//    assertEquals(2, list.size());
//    createAndCommitFoo(bar);
//    createAndCommitFoo(bar);
//    assertEquals(2, list.size());
//  }
//
//  @Test
//  public void testSizeReturnsCurrentSizeForNonLoadedArrayAfterChangesInDB() {
//    IDBObject bar = createAndCommitBar();
//    createAndCommitFoo(bar);
//    createAndCommitFoo(bar);
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    assertEquals(2, list.size());
//    createAndCommitFoo(bar);
//    createAndCommitFoo(bar);
//    assertEquals(4, list.size());
//  }
//
//  @Test
//  public void testSizeIssuesCountStarQueryIfArrayHasNotBeenLoaded() {
//    IDBObject bar = createAndCommitBar();
//    QueryExecutorSpy spy = new QueryExecutorSpy();
//    ReverseFkEntityCollectionImpl list = createList(bar, spy);
//    assertEquals(0, list.size());
//    assertTrue(spy.countCalled());
//    assertFalse(spy.selectCalled());
//    assertFalse(spy.updateCalled());
//  }
//
//  @Test
//  public void testSizeDoesNotIssueQueriesIfArrayHasBeenLoaded() {
//    IDBObject bar = createAndCommitBar();
//    createAndCommitFoo(bar);
//    createAndCommitFoo(bar);
//    QueryExecutorSpy spy = new QueryExecutorSpy();
//    ReverseFkEntityCollectionImpl list = createList(bar, spy);
//    list.load();
//    spy.reset();
//    assertEquals(2, list.size());
//    assertFalse(spy.anyCalled());
//  }
//
//  @Test
//  public void testGetThrowsIndexOutOfBoundsExceptionIfArgumentIsNegative() {
//    IDBObject bar = createAndCommitBar();
//    createAndCommitFoo(bar);
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    try {
//      list.get(-1);
//      fail("Expected an IndexOutOfBoundsException");
//    } catch (IndexOutOfBoundsException e) {
//      // Expected
//    }
//  }
//
//  @Test
//  public void testGetThrowsIndexOutOfBoundsExceptionIfArgumentIsGreaterThanSizeOfCollection() {
//    IDBObject bar = createAndCommitBar();
//    createAndCommitFoo(bar);
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    try {
//      list.get(2);
//      fail("Expected an IndexOutOfBoundsException");
//    } catch (IndexOutOfBoundsException e) {
//      // Expected
//    }
//  }
//
//  @Test
//  public void testGetThrowsIndexOutOfBoundsExceptionIfArgumentIsEqualToSizeOfCollection() {
//    IDBObject bar = createAndCommitBar();
//    createAndCommitFoo(bar);
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    try {
//      list.get(1);
//      fail("Expected an IndexOutOfBoundsException");
//    } catch (IndexOutOfBoundsException e) {
//      // Expected
//    }
//  }
//
//  @Test
//  public void testGetReturnsAppropriateElementIfArgumentIsValid() {
//    IDBObject bar = createAndCommitBar();
//    IDBObject foo = createAndCommitFoo(bar);
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    IDBObject fooFromDB = list.get(0);
//    assertEquals(foo.getColumnValue("id"), fooFromDB.getColumnValue("id"));
//  }
//
//  @Test
//  public void testIteratorHasNextReturnsFalseForEmptyList() {
//    IDBObject bar = createAndCommitBar();
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    assertFalse(list.iterator().hasNext());
//  }
//
//  @Test
//  public void testIteratorHasNextReturnsTrueAtStartOfNonEmptyList() {
//    IDBObject bar = createAndCommitBar();
//    IDBObject foo1 = createAndCommitFoo(bar);
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    assertTrue(list.iterator().hasNext());
//  }
//
//  @Test
//  public void testIteratorNextReturnsItemsInIdOrder() {
//    IDBObject bar = createAndCommitBar();
//    IDBObject foo1 = createAndCommitFoo(bar);
//    IDBObject foo2 = createAndCommitFoo(bar);
//    IDBObject foo3 = createAndCommitFoo(bar);
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    Iterator<IDBObject> it = list.iterator();
//    assertTrue(it.hasNext());
//    assertEquals(foo1.getColumnValue("id"), it.next().getColumnValue("id"));
//    assertTrue(it.hasNext());
//    assertEquals(foo2.getColumnValue("id"), it.next().getColumnValue("id"));
//    assertTrue(it.hasNext());
//    assertEquals(foo3.getColumnValue("id"), it.next().getColumnValue("id"));
//    assertFalse(it.hasNext());
//  }
//
//  @Test
//  public void testIteratorHasNextReturnsFalseAtEndOfList() {
//    IDBObject bar = createAndCommitBar();
//    IDBObject foo1 = createAndCommitFoo(bar);
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    Iterator<IDBObject> it = list.iterator();
//    assertTrue(it.hasNext());
//    it.next();
//    assertFalse(it.hasNext());
//  }
//
//  @Test
//  public void testIteratorRemoveThrowsUnsupportedOperationException() {
//    IDBObject bar = createAndCommitBar();
//    IDBObject foo1 = createAndCommitFoo(bar);
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    Iterator<IDBObject> it = list.iterator();
//    it.next();
//    try {
//      it.remove();
//      fail("Expected an UnsupportedOperationException");
//    } catch (UnsupportedOperationException e) {
//      // Expected
//    }
//  }
//
//  // TODO - AHK - Test for concurrent modification exceptions?
//
//  @Test
//  public void testAddThrowsIllegalStateExceptionIfOwningEntityHasNotYetBeenCommitted() {
//    IDBObject bar = new CachedDBObject((IDBType) TypeSystem.getByFullName("test.testdb.Bar"), true);
//    IDBObject foo = createFoo();
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    try {
//      list.add(foo);
//      fail("Expected add to throw an IllegalStateException");
//    } catch (IllegalStateException e) {
//      // Expected
//    }
//  }
//
//  @Test
//  public void testAddSetsFkPointerIfEntityIsAlreadyInThisCollection() {
//    IDBObject bar = createAndCommitBar();
//    IDBObject foo = createAndCommitFoo(bar);
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    assertEquals(1, list.size());
//    list.add(foo);
//    assertEquals(1, list.size());
//    assertSame(bar, foo.getFkValue("Bar_id"));
//  }
//
//  @Test
//  public void testAddThrowsIllegalArgumentExceptionIfEntityIsAlreadyInAnotherCollection() {
//    IDBObject bar1 = createAndCommitBar();
//    IDBObject bar2 = createAndCommitBar();
//    IDBObject foo = createFoo();
//    setBarId(bar2, foo);
//    update(foo);
//
//    ReverseFkEntityCollectionImpl list = createList(bar1);
//    try {
//      list.add(foo);
//      fail("Expected add to throw an IllegalArgumentException");
//    } catch (IllegalArgumentException e) {
//      // Expected
//    }
//  }
//
//  @Test
//  public void testAddThrowsIllegalArgumentExceptionIfEntityIsOfWrongType() {
//    IDBObject bar1 = createAndCommitBar();
//    IDBObject bar2 = createAndCommitBar();
//
//    ReverseFkEntityCollectionImpl list = createList(bar1);
//    try {
//      list.add(bar2);
//      fail("Expected add to throw an IllegalArgumentException");
//    } catch (IllegalArgumentException e) {
//      // Expected
//    }
//  }
//
//  @Test
//  public void testAddInsertsNewObjectInDatabaseAndSetsFkBackPointerIfObjectIsNotYetCommitted() {
//    IDBObject bar = createAndCommitBar();
//    IDBObject foo = createFoo();
//    assertNull(foo.getColumnValue("id"));
//    assertTrue(foo.isNew());
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    list.add(foo);
//    assertNotNull(foo.getColumnValue("id"));
//    assertFalse(foo.isNew());
//    assertSame(bar, foo.getFkValue("Bar_id"));
//    assertEquals(1, countMatchesInDB(foo, bar));
//  }
//
//  @Test
//  public void testAddUpdatesFkInDatabaseAndSetsFkBackPointerIfObjectHasAlreadyBeenPersisted() {
//    IDBObject bar = createAndCommitBar();
//    IDBObject foo = createFoo();
//    update(foo);
//    assertEquals(0, countMatchesInDB(foo, bar));
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    list.add(foo);
//    assertEquals(bar.getId(), foo.getColumnValue("Bar_id"));
//    assertSame(bar, foo.getFkValue("Bar_id"));
//    assertEquals(1, countMatchesInDB(foo, bar));
//  }
//
//  @Test
//  public void testAddShowsElementInResultsImmediatelyIfResultsWereNotPreviouslyLoaded() {
//    IDBObject bar = createAndCommitBar();
//    IDBObject foo1 = createAndCommitFoo(bar);
//    IDBObject foo2 = createFoo();
//    update(foo2);
//    IDBObject foo3 = createAndCommitFoo(bar);
//
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    assertEquals(2, list.size());
//    list.add(foo2);
//    assertEquals(3, list.size());
//    assertEquals(foo1.getColumnValue("id"), list.get(0).getColumnValue("id"));
//    assertEquals(foo2.getColumnValue("id"), list.get(1).getColumnValue("id"));
//    assertEquals(foo3.getColumnValue("id"), list.get(2).getColumnValue("id"));
//    // TODO - AHK - Test that it's not the same object?
//  }
//
//  @Test
//  public void testAddShowsElementInResultsImmediatelyIfResultsWerePreviouslyLoaded() {
//    IDBObject bar = createAndCommitBar();
//    IDBObject foo1 = createAndCommitFoo(bar);
//    IDBObject foo2 = createFoo();
//    update(foo2);
//    IDBObject foo3 = createAndCommitFoo(bar);
//
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    assertEquals(2, list.size());
//    list.load();
//    list.add(foo2);
//    assertEquals(3, list.size());
//    assertEquals(foo1.getColumnValue("id"), list.get(0).getColumnValue("id"));
//    assertEquals(foo3.getColumnValue("id"), list.get(1).getColumnValue("id"));
//    assertEquals(foo2.getColumnValue("id"), list.get(2).getColumnValue("id"));
//    assertSame(foo2, list.get(2));
//  }
//
//  @Test
//  public void testUnloadWillUnloadCachedData() {
//    IDBObject bar = createAndCommitBar();
//    createAndCommitFoo(bar);
//    createAndCommitFoo(bar);
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    list.load();
//    assertEquals(2, list.size());
//    createAndCommitFoo(bar);
//    createAndCommitFoo(bar);
//    assertEquals(2, list.size());
//    list.unload();
//    assertEquals(4, list.size());
//    list.load();
//    assertEquals(4, list.size());
//  }
//
//  // ----------------------- Tests for remove()
//
//  @Test
//  public void testRemoveWillThrowIllegalArgumentExceptionIfElementIsNotAMemberOfThisArray() {
//    IDBObject bar = createAndCommitBar();
//    IDBObject bar2 = createAndCommitBar();
//    IDBObject foo = createAndCommitFoo(bar);
//    try {
//      createList(bar2).remove(foo);
//      fail("Expected an IllegalArgumentException");
//    } catch (IllegalArgumentException e) {
//      // Expected
//    }
//  }
//
//  @Test
//  public void testRemoveWillThrowIllegalStateExceptionIfOwnerIsNew() {
//    IDBObject bar = new CachedDBObject((IDBType) TypeSystem.getByFullName("test.testdb.Bar"), true);
//    IDBObject foo = createFoo();
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    try {
//      list.remove(foo);
//      fail("Expected add to throw an IllegalStateException");
//    } catch (IllegalStateException e) {
//      // Expected
//    }
//  }
//
//  @Test
//  public void testRemoveWillThrowIllegalArgumentExceptionIfElementIsOfWrongType() {
//    IDBObject bar = createAndCommitBar();
//    IDBObject bar2 = createAndCommitBar();
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    try {
//      list.remove(bar2);
//      fail("Expected remove to throw an IllegalStateException");
//    } catch (IllegalArgumentException e) {
//      // Expected
//    }
//  }
//
//  @Test
//  public void testRemoveWillImmediatelyUpdateDatabaseIfArrayNotLoadedYet() {
//    IDBObject bar = createAndCommitBar();
//    IDBObject foo = createAndCommitFoo(bar);
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    list.unload();
//    assertEquals(1, countMatchesInDB(foo, bar));
//    list.remove(foo);
//    assertEquals(0, countMatchesInDB(foo, bar));
//  }
//
//  @Test
//  public void testRemoveWillImmediatelyUpdateDatabaseAndRemoveFromCachedResultsIfArrayLoaded() {
//    IDBObject bar = createAndCommitBar();
//    IDBObject foo = createAndCommitFoo(bar);
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    list.load();
//    assertEquals(1, countMatchesInDB(foo, bar));
//    list.remove(foo);
//    assertEquals(0, countMatchesInDB(foo, bar));
//    assertEquals(0, list.size());
//    assertFalse(list.iterator().hasNext());
//  }
//
//  @Test
//  public void testRemoveWillNullOutFkColumnOnElement() {
//    IDBObject bar = createAndCommitBar();
//    IDBObject foo = createAndCommitFoo(bar);
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    assertEquals(bar.getId(), foo.getColumnValue("Bar_id"));
//    list.remove(foo);
//    assertNull(foo.getColumnValue("Bar_id"));
//  }
//
//  @Test
//  public void testRemoveWillNullOutCachedFkObjectOnElement() {
//    IDBObject bar = createAndCommitBar();
//    IDBObject foo = createAndCommitFoo(bar);
//    ReverseFkEntityCollectionImpl list = createList(bar);
//    assertNotNull(foo.getFkValue("Bar_id"));
//    list.remove(foo);
//    assertNull(foo.getFkValue("Bar_id"));
//  }

  // ----------------------------- Helper Methods/Classes

//  private int countMatchesInDB(IDBObject foo, IDBObject bar) {
//    String sql = SimpleSqlBuilder.select("count(*) as count").from(foo.getDBTable()).where("\"id\" = " + foo.getId() + " AND \"Bar_id\" = " + bar.getId()).toString();
//    return new QueryExecutorImpl(getDB()).count("", sql);
//  }

  private static class QueryExecutorSpy implements QueryExecutor {

    private QueryExecutorImpl _delegate;
    private String _count;
    private String _select;
    private String _update;

    private QueryExecutorSpy() {
      _delegate = new QueryExecutorImpl(getDB());
    }

    @Override
    public int count(String profilerTag, String sqlStatement, IPreparedStatementParameter... parameters) {
      _count = sqlStatement;
      return _delegate.count(profilerTag, sqlStatement, parameters);
    }

    @Override
    public List<IDBObject> selectEntity(String profilerTag, IDBType targetType, String sqlStatement, IPreparedStatementParameter... parameters) {
      _select = sqlStatement;
      return _delegate.selectEntity(profilerTag, targetType, sqlStatement, parameters);
    }

    @Override
    public void update(String profilerTag, String sqlStatement, IPreparedStatementParameter... parameters) {
      _update = sqlStatement;
      update(profilerTag, sqlStatement, parameters);
    }

    public boolean countCalled() {
      return _count != null;
    }

    public boolean selectCalled() {
      return _select != null;
    }

    public boolean updateCalled() {
      return _update != null;
    }

    public boolean anyCalled() {
      return countCalled() || selectCalled() || updateCalled();
    }

    public void reset() {
      _count = null;
      _select = null;
      _update = null;
    }
  }
}
