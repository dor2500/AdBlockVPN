package com.adblocker.vpn.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.adblocker.vpn.data.model.NetworkIdentifierType;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ExcludedNetworkDao_Impl implements ExcludedNetworkDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ExcludedNetworkEntity> __insertionAdapterOfExcludedNetworkEntity;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<ExcludedNetworkEntity> __deletionAdapterOfExcludedNetworkEntity;

  private final EntityDeletionOrUpdateAdapter<ExcludedNetworkEntity> __updateAdapterOfExcludedNetworkEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  public ExcludedNetworkDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfExcludedNetworkEntity = new EntityInsertionAdapter<ExcludedNetworkEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `excluded_networks` (`id`,`displayName`,`identifierType`,`identifierValue`,`enabled`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ExcludedNetworkEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getDisplayName());
        final String _tmp = __converters.fromType(entity.getIdentifierType());
        statement.bindString(3, _tmp);
        statement.bindString(4, entity.getIdentifierValue());
        final int _tmp_1 = entity.getEnabled() ? 1 : 0;
        statement.bindLong(5, _tmp_1);
      }
    };
    this.__deletionAdapterOfExcludedNetworkEntity = new EntityDeletionOrUpdateAdapter<ExcludedNetworkEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `excluded_networks` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ExcludedNetworkEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfExcludedNetworkEntity = new EntityDeletionOrUpdateAdapter<ExcludedNetworkEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `excluded_networks` SET `id` = ?,`displayName` = ?,`identifierType` = ?,`identifierValue` = ?,`enabled` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ExcludedNetworkEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getDisplayName());
        final String _tmp = __converters.fromType(entity.getIdentifierType());
        statement.bindString(3, _tmp);
        statement.bindString(4, entity.getIdentifierValue());
        final int _tmp_1 = entity.getEnabled() ? 1 : 0;
        statement.bindLong(5, _tmp_1);
        statement.bindLong(6, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM excluded_networks WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final ExcludedNetworkEntity entity,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfExcludedNetworkEntity.insertAndReturnId(entity);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final ExcludedNetworkEntity entity,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfExcludedNetworkEntity.handle(entity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final ExcludedNetworkEntity entity,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfExcludedNetworkEntity.handle(entity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ExcludedNetworkEntity>> observeAll() {
    final String _sql = "SELECT * FROM excluded_networks ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"excluded_networks"}, new Callable<List<ExcludedNetworkEntity>>() {
      @Override
      @NonNull
      public List<ExcludedNetworkEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfIdentifierType = CursorUtil.getColumnIndexOrThrow(_cursor, "identifierType");
          final int _cursorIndexOfIdentifierValue = CursorUtil.getColumnIndexOrThrow(_cursor, "identifierValue");
          final int _cursorIndexOfEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "enabled");
          final List<ExcludedNetworkEntity> _result = new ArrayList<ExcludedNetworkEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExcludedNetworkEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final NetworkIdentifierType _tmpIdentifierType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfIdentifierType);
            _tmpIdentifierType = __converters.toType(_tmp);
            final String _tmpIdentifierValue;
            _tmpIdentifierValue = _cursor.getString(_cursorIndexOfIdentifierValue);
            final boolean _tmpEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfEnabled);
            _tmpEnabled = _tmp_1 != 0;
            _item = new ExcludedNetworkEntity(_tmpId,_tmpDisplayName,_tmpIdentifierType,_tmpIdentifierValue,_tmpEnabled);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getAllEnabled(final Continuation<? super List<ExcludedNetworkEntity>> $completion) {
    final String _sql = "SELECT * FROM excluded_networks WHERE enabled = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ExcludedNetworkEntity>>() {
      @Override
      @NonNull
      public List<ExcludedNetworkEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfIdentifierType = CursorUtil.getColumnIndexOrThrow(_cursor, "identifierType");
          final int _cursorIndexOfIdentifierValue = CursorUtil.getColumnIndexOrThrow(_cursor, "identifierValue");
          final int _cursorIndexOfEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "enabled");
          final List<ExcludedNetworkEntity> _result = new ArrayList<ExcludedNetworkEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExcludedNetworkEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final NetworkIdentifierType _tmpIdentifierType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfIdentifierType);
            _tmpIdentifierType = __converters.toType(_tmp);
            final String _tmpIdentifierValue;
            _tmpIdentifierValue = _cursor.getString(_cursorIndexOfIdentifierValue);
            final boolean _tmpEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfEnabled);
            _tmpEnabled = _tmp_1 != 0;
            _item = new ExcludedNetworkEntity(_tmpId,_tmpDisplayName,_tmpIdentifierType,_tmpIdentifierValue,_tmpEnabled);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
