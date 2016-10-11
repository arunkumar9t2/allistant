package com.arun.allistant;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.arun.allistant.shared.Constants;

import java.util.LinkedList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by Arun on 06/10/2016.
 */

public class AssistantOpenerService extends AccessibilityService {
    private final LinkedList<String> tasksQueue = new LinkedList<>();
    private final AccessibilityReceiver accessibilityReceiver = new AccessibilityReceiver();

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        final IntentFilter filter = new IntentFilter(Constants.ACTION_LAUNCH_ASSISTANT);
        filter.addAction(Constants.ACTION_SET_VOICE_MESSAGE);
        LocalBroadcastManager.getInstance(this).registerReceiver(accessibilityReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(accessibilityReceiver);
    }

    private boolean isAllo(@Nullable AccessibilityEvent event) {
        return event != null && event.getPackageName() != null
                && Constants.ALLO.equalsIgnoreCase(event.getPackageName().toString());
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!isAllo(event)) {
            // Only handle event when its Allo app.
            return;
        }

        Timber.d(event.toString());

        while (!tasksQueue.isEmpty()) {
            final String action = tasksQueue.getFirst();
            if (action.equalsIgnoreCase(Constants.TASK_ENSURE_CONVERSATION_LIST)) {
                if (ensureConversationList(getRootInActiveWindow(), event)) {
                    tasksQueue.remove(action);
                } else {
                    Timber.d("Deferred %s", action);
                    break;
                }
            } else if (action.equalsIgnoreCase(Constants.TASK_CLICK_ASSISTANT)) {
                if (launchAssistantActivity(getRootInActiveWindow(), event)) {
                    tasksQueue.remove(action);
                } else {
                    Timber.d("Deferred %s", action);
                    break;
                }
            } /*else if (action.equalsIgnoreCase(Constants.TASK_GET_VOICE)) {
                startActivity(new Intent(this, VoiceRecognizeActivity.class));
                if (beginVoiceRecognition(getRootInActiveWindow(), event)) {
                    tasksQueue.remove(action);
                } else {
                    Timber.d("Deferred %s", action);
                    break;
                }
            } else if (action.equalsIgnoreCase(Constants.TASK_SET_TEXT)) {
                if (setTextInComposeWidget(getRootInActiveWindow(), event, textToWrite)) {
                    tasksQueue.remove(action);
                } else {
                    Timber.d("Deferred %s", action);
                    break;
                }
            }*/
        }
    }


    private boolean ensureConversationList(@Nullable AccessibilityNodeInfo rootInActiveWindow, @Nullable AccessibilityEvent event) {
        if (rootInActiveWindow == null || event == null) {
            return false;
        }

        if (event.getClassName() != null && event.getClassName().toString().equalsIgnoreCase(Constants.CONVERSATION_LIST)) {
            return true;
        } else {
            final List<AccessibilityNodeInfo> nodeInfos = rootInActiveWindow.findAccessibilityNodeInfosByText(Constants.NAVIGATE_UP);
            for (final AccessibilityNodeInfo node : nodeInfos) {
                if (node.getContentDescription() != null && node.getContentDescription().toString().contains(Constants.NAVIGATE_UP)) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    // We return false until we reach the conversation list activity.
                    return false;
                }
            }
        }
        return false;
    }

    private boolean launchAssistantActivity(@Nullable AccessibilityNodeInfo rootInActiveWindow, @Nullable AccessibilityEvent event) {
        if (rootInActiveWindow == null || event == null) {
            return false;
        }

        // Check if we are in the correct activity
        if (event.getClassName() != null && event.getClassName().toString().equalsIgnoreCase(Constants.CONVERSATION_LIST)) {
            // We are in the conversation list activity, so we will find the Google assistant item and trigger a click
            final List<AccessibilityNodeInfo> nodeInfos = rootInActiveWindow.findAccessibilityNodeInfosByText(Constants.ASSISTANT);
            for (final AccessibilityNodeInfo node : nodeInfos) {
                if (node.getContentDescription() != null && node.getContentDescription().toString().contains(Constants.ASSISTANT)) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean setTextInComposeWidget(@Nullable AccessibilityNodeInfo rootInActiveWindow, @Nullable AccessibilityEvent event, @Nullable String textToSet) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return true;
        }
        if (rootInActiveWindow == null || event == null || TextUtils.isEmpty(textToSet)) {
            return false;
        }
        if (event.getClassName() != null && event.getClassName().toString().equalsIgnoreCase(Constants.CONVERSATION)) {
            final AccessibilityNodeInfo focusedNode = rootInActiveWindow.findFocus(AccessibilityNodeInfo.ACTION_FOCUS);
            if (focusedNode != null) {
                final String id = focusedNode.getViewIdResourceName();
                final List<AccessibilityNodeInfo> editCandidates = rootInActiveWindow.findAccessibilityNodeInfosByViewId(id);
                for (final AccessibilityNodeInfo node : editCandidates) {
                    if (node != null) {
                        final ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        final ClipData clip = ClipData.newPlainText("Voice", textToSet);
                        clipboard.setPrimaryClip(clip);
                        node.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onInterrupt() {
    }

    /**
     * Broadcast receiver to communicate with this service. Responsible to populating {@link #tasksQueue}
     * so that appropriate action is taken in {@link #onAccessibilityEvent(AccessibilityEvent)}.
     */
    public class AccessibilityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.d("Received intent in receiver %s", intent.toString());
            if (intent.getAction().equalsIgnoreCase(Constants.ACTION_LAUNCH_ASSISTANT)) {
                // Launch Allo
                startActivity(getPackageManager().getLaunchIntentForPackage(Constants.ALLO));

                tasksQueue.clear();
                tasksQueue.add(Constants.TASK_ENSURE_CONVERSATION_LIST);
                tasksQueue.add(Constants.TASK_CLICK_ASSISTANT);
            }
        }
    }
}
