package org.omg.CosEventChannelAdmin.jni;

public abstract class ProxyPushConsumerHelper extends org.omg.CosEventChannelAdmin.ProxyPushConsumerHelper
{
  public static org.omg.CosEventChannelAdmin.ProxyPushConsumer narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null) {
      return null;
    }
    else if (obj instanceof org.omg.CosEventChannelAdmin.jni._ProxyPushConsumerStub) {
      return (org.omg.CosEventChannelAdmin.ProxyPushConsumer)obj;
    }
    else if (!obj._is_a(id())) {
      throw new org.omg.CORBA.BAD_PARAM();
    }
    else if (obj instanceof omnijni.ObjectImpl) {
      org.omg.CosEventChannelAdmin.jni._ProxyPushConsumerStub stub = new org.omg.CosEventChannelAdmin.jni._ProxyPushConsumerStub();
      long ref = ((omnijni.ObjectImpl)obj)._get_object_ref();
      stub._set_object_ref(ref);
      return (org.omg.CosEventChannelAdmin.ProxyPushConsumer)stub;
    }
    else {
      org.omg.CORBA.ORB orb = ((org.omg.CORBA.portable.ObjectImpl)obj)._orb();
      String ior = orb.object_to_string(obj);
      return narrow(omnijni.ORB.string_to_object(ior));
    }
  }
}
