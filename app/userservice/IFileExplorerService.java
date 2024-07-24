/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package com.magicianguo.fileexplorer.userservice;
public interface IFileExplorerService extends android.os.IInterface
{
  /** Default implementation for IFileExplorerService. */
  public static class Default implements com.magicianguo.fileexplorer.userservice.IFileExplorerService
  {
    @Override public java.util.List<com.magicianguo.fileexplorer.bean.BeanFile> listFiles(java.lang.String path) throws android.os.RemoteException
    {
      return null;
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.magicianguo.fileexplorer.userservice.IFileExplorerService
  {
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.magicianguo.fileexplorer.userservice.IFileExplorerService interface,
     * generating a proxy if needed.
     */
    public static com.magicianguo.fileexplorer.userservice.IFileExplorerService asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.magicianguo.fileexplorer.userservice.IFileExplorerService))) {
        return ((com.magicianguo.fileexplorer.userservice.IFileExplorerService)iin);
      }
      return new com.magicianguo.fileexplorer.userservice.IFileExplorerService.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(descriptor);
      }
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
      }
      switch (code)
      {
        case TRANSACTION_listFiles:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          java.util.List<com.magicianguo.fileexplorer.bean.BeanFile> _result = this.listFiles(_arg0);
          reply.writeNoException();
          _Parcel.writeTypedList(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements com.magicianguo.fileexplorer.userservice.IFileExplorerService
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      @Override public java.util.List<com.magicianguo.fileexplorer.bean.BeanFile> listFiles(java.lang.String path) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.util.List<com.magicianguo.fileexplorer.bean.BeanFile> _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(path);
          boolean _status = mRemote.transact(Stub.TRANSACTION_listFiles, _data, _reply, 0);
          _reply.readException();
          _result = _reply.createTypedArrayList(com.magicianguo.fileexplorer.bean.BeanFile.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
    }
    static final int TRANSACTION_listFiles = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
  }
  public static final java.lang.String DESCRIPTOR = "com.magicianguo.fileexplorer.userservice.IFileExplorerService";
  public java.util.List<com.magicianguo.fileexplorer.bean.BeanFile> listFiles(java.lang.String path) throws android.os.RemoteException;
  /** @hide */
  static class _Parcel {
    static private <T> T readTypedObject(
        android.os.Parcel parcel,
        android.os.Parcelable.Creator<T> c) {
      if (parcel.readInt() != 0) {
          return c.createFromParcel(parcel);
      } else {
          return null;
      }
    }
    static private <T extends android.os.Parcelable> void writeTypedObject(
        android.os.Parcel parcel, T value, int parcelableFlags) {
      if (value != null) {
        parcel.writeInt(1);
        value.writeToParcel(parcel, parcelableFlags);
      } else {
        parcel.writeInt(0);
      }
    }
    static private <T extends android.os.Parcelable> void writeTypedList(
        android.os.Parcel parcel, java.util.List<T> value, int parcelableFlags) {
      if (value == null) {
        parcel.writeInt(-1);
      } else {
        int N = value.size();
        int i = 0;
        parcel.writeInt(N);
        while (i < N) {
    writeTypedObject(parcel, value.get(i), parcelableFlags);
          i++;
        }
      }
    }
  }
}
