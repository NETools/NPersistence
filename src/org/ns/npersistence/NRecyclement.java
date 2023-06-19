package org.ns.npersistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

class NRecyclement {

    private static NRecyclement recyclementInstance;

    public static NRecyclement getDefault() {
        if (recyclementInstance == null)
            recyclementInstance = new NRecyclement();
        return recyclementInstance;
    }


    private HashMap<Object, Stack<String>> deletionStack;

    private NRecyclement() {
        deletionStack = new HashMap<>();
    }


    public void add(Object proxyInstance, Object element, String fieldName) throws Exception {

        var stackField =
                (Stack<Object>) proxyInstance.getClass().getDeclaredField("__recycle__" + fieldName).get(proxyInstance);

        if (stackField == null) {
            proxyInstance.getClass().getDeclaredField("__recycle__" + fieldName).set(proxyInstance, new Stack<>());
            stackField =
                    (Stack<Object>) proxyInstance.getClass().getDeclaredField("__recycle__" + fieldName).get(proxyInstance);
        }

        if (!deletionStack.containsKey(proxyInstance))
            deletionStack.put(proxyInstance, new Stack<>());

        deletionStack.get(proxyInstance).push(fieldName);

        var localCopy =
                NEntityUpdateHandler.getDefault().getCurrentSession().makeLocal(element);

        stackField.push(localCopy);
    }

    public void rollback(Object proxyInstance) throws Exception {
        if (deletionStack.get(proxyInstance).size() < 1) {
            NDebugOutputHandler.getDefault().handle(82, "[ROLLBACK] FAILED TO ROLLBACK: THE STACK IS EMPTY!");
            return;
        }

        NDebugOutputHandler.getDefault().handle(80, "[ROLLBACK] STARTING ROLLBACK!");
        var rollBackIterableName =
                deletionStack.get(proxyInstance).pop();

        var stackField =
                (Stack<Object>) proxyInstance.getClass().getDeclaredField("__recycle__" + rollBackIterableName).get(proxyInstance);

        var listField =
                proxyInstance.getClass().getSuperclass().getDeclaredField(rollBackIterableName);
        listField.setAccessible(true);

        ((NArrayList<Object>) listField
                .get(proxyInstance)).add(stackField.pop());
    }


}
