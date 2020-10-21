package abi39_0_0.expo.modules.notifications.notifications.presentation.builders;

import android.app.PendingIntent;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;
import expo.modules.notifications.notifications.model.NotificationAction;
import expo.modules.notifications.notifications.model.NotificationCategory;
import expo.modules.notifications.notifications.model.NotificationContent;
import expo.modules.notifications.notifications.model.TextInputNotificationAction;
import expo.modules.notifications.notifications.service.SharedPreferencesNotificationCategoriesStore;
import expo.modules.notifications.service.NotificationsService;

public class CategoryAwareNotificationBuilder extends ExpoNotificationBuilder {
  protected SharedPreferencesNotificationCategoriesStore mStore;

  public CategoryAwareNotificationBuilder(Context context, @NonNull SharedPreferencesNotificationCategoriesStore store) {
    super(context);
    mStore = store;
  }

  @Override
  protected NotificationCompat.Builder createBuilder() {
    NotificationCompat.Builder builder = super.createBuilder();

    NotificationContent content = getNotificationContent();

    String categoryIdentifier = content.getCategoryId();
    if (categoryIdentifier != null) {
      List<NotificationAction> actions = Collections.emptyList();
      try {
        NotificationCategory category = mStore.getNotificationCategory(categoryIdentifier);
        if (category != null) {
          actions = category.getActions();
        }
      } catch (ClassNotFoundException | IOException e) {
        Log.e("expo-notifications", String.format("Could not read category with identifier: %s. %s", categoryIdentifier, e.getMessage()));
        e.printStackTrace();
      }
      for (NotificationAction action : actions) {
        if (action instanceof TextInputNotificationAction) {
          builder.addAction(buildTextInputAction((TextInputNotificationAction) action));
        } else {
          builder.addAction(buildButtonAction(action));
        }
      }
    }

    return builder;
  }

  protected NotificationCompat.Action buildButtonAction(@NonNull NotificationAction action) {
    PendingIntent intent = NotificationsService.Companion.createNotificationResponseIntent(getContext(), getNotification(), action);
    return new NotificationCompat.Action.Builder(super.getIcon(), action.getTitle(), intent).build();
  }

  protected NotificationCompat.Action buildTextInputAction(@NonNull TextInputNotificationAction action) {
    PendingIntent intent = NotificationsService.Companion.createNotificationResponseIntent(getContext(), getNotification(), action);
    RemoteInput remoteInput = new RemoteInput.Builder(NotificationsService.USER_TEXT_RESPONSE_KEY)
      .setLabel(action.getPlaceholder())
      .build();

    return new NotificationCompat.Action.Builder(super.getIcon(), action.getTitle(), intent).addRemoteInput(remoteInput).build();
  }
}
