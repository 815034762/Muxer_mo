package cangwang.com.base.modulebus;

import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zjl on 16/10/19.
 */

public class ModuleBus {
    private static final String TAG = "ModuleBus";

    private static Map<Object,HashMap<String,Method>> moduleEventMethods = new HashMap<>();
    private static Map<Class<?>,ArrayList<Object>> moduleClients = new HashMap<>();

    private static ModuleBus instance;

    public static ModuleBus getInstance(){
        if(instance == null){
            synchronized (ModuleBus.class){
                if (instance == null){
                    instance = new ModuleBus();
                }
            }
        }
        return instance;
    }

    /**
     * 注册
     * @param client
     */
    public void register(Object client){
        if(client == null) return;

        Class<?> orginalClass = client.getClass();
        if(orginalClass == null) return;


        Method[] methods = orginalClass.getMethods();

        for(Method method:methods){
            ModuleEvent event = method.getAnnotation(ModuleEvent.class);
            if(event !=null){
                Class<?> clientClass = event.coreClientClass();

                addClient(clientClass,client);
                addEventMethod(client,clientClass,method);
            }
        }
    }

    private void addClient(Class<?> clientClass,Object client){
        ArrayList<Object> clientList = moduleClients.get(clientClass);
        if (clientList !=null) {
            for (Object c : clientList) {
                if (c.getClass().getName().equals(client.getClass().getName())) {
                        clientList.remove(c);
                }
            }
        }

        if (clientList == null)
            clientList = new ArrayList<>();

        clientList.add(client);
        moduleClients.put(clientClass,clientList);
    }

    private void addEventMethod(Object client,Class<?> clientClass, Method m){
        HashMap<String,Method> methods = moduleEventMethods.get(client);

        if(methods == null){
            methods = new HashMap<>();
            moduleEventMethods.put(clientClass,methods);
        }
        methods.put(m.getName(),m);
    }

    /**
     * 注销
     * @param client
     */
    public void unregister(Object client){
        if(client == null) return;

        moduleClients.remove(client.getClass());
    }

    public ArrayList<Object> getClient(Class<?> clientClass){
        if(clientClass == null) return null;
        ArrayList<Object> clientList = moduleClients.get(clientClass);
        if(clientList != null){
            clientList = new ArrayList<>(clientList);
        }

        return clientList;
    }

    public void post(Class<?> clientClass,String methodName,Object...args){
        if(clientClass == null || methodName == null ||methodName.length() == 0) return;

        ArrayList<Object> clientList = getClient(clientClass);
        if(clientList == null) return;

        try{
            HashMap<String,Method> methods = moduleEventMethods.get(clientClass);
            Method method = methods.get(methodName);
            if(method == null){
                Log.e(TAG,"cannot find client method"+methodName +"for args["+args.length+"]" + Arrays.toString(args));
                return;
            }else if(method.getParameterTypes() == null){
                Log.e(TAG,"cannot find client method param:"+method.getParameterTypes() +"for args["+args.length+"]" + Arrays.toString(args));
                return;
            }else if(method.getParameterTypes().length != args.length){
                Log.e(TAG,"method "+methodName +" param number not matched:method("+method.getParameterTypes().length+"), args(" + args.length+")");
                return;
            }

            for(Object  c: clientList){
                try{
                    method.invoke(c,args);
                }catch (Throwable e){
                    Log.e(TAG,"Notifiy client method invoke error.",e);
                }
            }

        }catch (Throwable e){
            Log.e(TAG,"Notify client error",e);
        }
    }

}
