package de.abring.remotecontrol.remote;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.abring.remotecontrol.MainActivity;
import de.abring.remotecontrol.R;
import de.abring.remotecontrol.internet.DeviceCommunicator;

public abstract class RemoteConfigurator {

    private static final String TAG = "RemoteConfigurator";
    private final Context context;
    private int remote;
    private int key;
    private int maxKey;

    View dialogViewKey;

    AlertDialog addDialog;
    AlertDialog finFbDialog;
    AlertDialog endDialog;

    public List<String> keys;

    private final int maxRemote = 4;

    public RemoteConfigurator(Context context) {
        this.context = context;
        this.keys = new ArrayList<>(Arrays.asList(context.getResources().getStringArray(R.array.remote_configuration_keys)));

        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        dialogViewKey = inflater.inflate(R.layout.dialog_program, null);

        AlertDialog.Builder addDialogBuilder = new AlertDialog.Builder(context);

        addDialogBuilder
                .setTitle(R.string.remote_configuration_add_key_title)
//                .setMessage(styledText)
                .setView(dialogViewKey)
                .setPositiveButton(R.string.remote_configuration_add_key_positive, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        key++;
                        if (key > maxKey) {
                            new DeviceCommunicator.AddRemote(DeviceCommunicator.AddRemote.FIN_FB) {
                                @Override
                                public void finished(boolean success, String content) {
                                    finFB();
                                }
                            };
                        } else {
                            new DeviceCommunicator.AddRemote(DeviceCommunicator.AddRemote.KEY) {
                                @Override
                                public void finished(boolean success, String content) {
                                    addKey();
                                }
                            };
                        }
                    }
                })
                .setNegativeButton(R.string.remote_configuration_add_key_negative, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (remote == 0) {
                            maxKey = key;
                        }
                        new DeviceCommunicator.AddRemote(DeviceCommunicator.AddRemote.FIN_FB) {
                            @Override
                            public void finished(boolean success, String content) {
                                finFB();
                            }
                        };
                    }
                }).setNeutralButton(R.string.remote_configuration_add_key_neutral, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finished();
            }
        });

        addDialog = addDialogBuilder.create();
        addDialog.setCancelable(false);
        addDialog.setCanceledOnTouchOutside(false);

        AlertDialog.Builder finFbBuilder = new AlertDialog.Builder(context);

        finFbBuilder
                .setTitle(R.string.remote_configuration_fin_fb_title)
                .setMessage(R.string.remote_configuration_fin_fb_message)
                .setPositiveButton(R.string.remote_configuration_fin_fb_positive, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        key = 0;
                        remote++;
                        if (remote > maxRemote) {
                            new DeviceCommunicator.AddRemote(DeviceCommunicator.AddRemote.END) {
                                @Override
                                public void finished(boolean success, String content) {
                                    end(content.startsWith("ok"));
                                }
                            };
                        } else {
                            new DeviceCommunicator.AddRemote(DeviceCommunicator.AddRemote.NEU_FB) {
                                @Override
                                public void finished(boolean success, String content) {
                                    addKey();
                                }
                            };
                        }
                    }
                })
                .setNegativeButton(R.string.remote_configuration_fin_fb_negative, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new DeviceCommunicator.AddRemote(DeviceCommunicator.AddRemote.END) {
                            @Override
                            public void finished(boolean success, String content) {
                                end(content.startsWith("ok"));
                            }
                        };
                    }
                })
                .setNeutralButton(R.string.remote_configuration_fin_fb_neutral, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finished();
                    }
                });

        finFbDialog = finFbBuilder.create();
        finFbDialog.setCancelable(false);
        finFbDialog.setCanceledOnTouchOutside(false);

        AlertDialog.Builder endDialogBuilder = new AlertDialog.Builder(context);
        endDialogBuilder
                .setPositiveButton(R.string.remote_configuration_end_positive, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finished();
                    }
                })
                .setNegativeButton(R.string.remote_configuration_end_negative, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        start();
                    }
                });

        endDialog = endDialogBuilder.create();

        endDialog.setCancelable(false);
        endDialog.setCanceledOnTouchOutside(false);

    }

    public void chooseKeys() {

        final CharSequence[] recKeys = context.getResources().getTextArray(R.array.remote_configuration_keys);

        final boolean[] active = new boolean[recKeys.length];

        for (int i = 0; i < active.length; i++) {
            active[i] = keys.contains(recKeys[i]);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder
                .setTitle(R.string.main_activiy_keys_chooser_title)
                .setMultiChoiceItems(recKeys, active, new DialogInterface.OnMultiChoiceClickListener() {
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        Log.d(TAG, "onClick: " + recKeys[which]);
                        active[which] = isChecked;
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        keys.clear();
                        for (int i = 0; i < active.length; i++) {
                            if (active[i]) {
                                keys.add(recKeys[i].toString());
                            }
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    //Alles auf Anfang
    public void start() {
        remote = 0;
        key = 0;

        if (keys.isEmpty()) {
            maxKey = Integer.MAX_VALUE;
        } else {
            maxKey = keys.size() - 1;
        }

        new DeviceCommunicator.AddRemote(DeviceCommunicator.AddRemote.START) {
            @Override
            public void finished(boolean success, String content) {
                addKey();
            }
        };
    }

    //Neuen Key einlesen
    private void addKey() {

        String remoteText;
        if (remote == 0) {
            remoteText = context.getResources().getString(R.string.remote_configuration_original_remote);
        } else {
            remoteText = context.getResources().getString(R.string.remote_configuration_remote) + " " + String.valueOf(remote);
        }
        TextView remoteTextView = (TextView) dialogViewKey.findViewById(R.id.textViewRemote);
        remoteTextView.setText(remoteText);

        String keyText;
        if (keys.isEmpty()) {
            keyText = String.valueOf(key);
        } else {
            keyText = keys.get(key);
        }
        TextView keyTextView = (TextView) dialogViewKey.findViewById(R.id.textViewKey);
        keyTextView.setText(keyText);

        ImageView keyImageView = (ImageView) dialogViewKey.findViewById(R.id.imageViewKeyImage);
        keyImageView.setImageResource(getRemoteResource(keyText));

        addDialog.show();
    }

    private int getRemoteResource(String key) {
        switch (key) {
            case "Power":
                return R.drawable.ic_remote_power;
            case "Programm hoch":
                return R.drawable.ic_remote_prog_up;
            case "Programm runter":
                return R.drawable.ic_remote_prog_down;
            case "Lauter":
                return R.drawable.ic_remote_vol_up;
            case "Leiser":
                return R.drawable.ic_remote_vol_down;
            case "Taste 0":
                return R.drawable.ic_remote_0;
            case "Taste 1":
                return R.drawable.ic_remote_1;
            case "Taste 2":
                return R.drawable.ic_remote_2;
            case "Taste 3":
                return R.drawable.ic_remote_3;
            case "Taste 4":
                return R.drawable.ic_remote_4;
            case "Taste 5":
                return R.drawable.ic_remote_5;
            case "Taste 6":
                return R.drawable.ic_remote_6;
            case "Taste 7":
                return R.drawable.ic_remote_7;
            case "Taste 8":
                return R.drawable.ic_remote_8;
            case "Taste 9":
                return R.drawable.ic_remote_9;
            case "Pfeil hoch":
                return R.drawable.ic_remote_up;
            case "Pfeil runter":
                return R.drawable.ic_remote_down;
            case "Pfeil rechts":
                return R.drawable.ic_remote_right;
            case "Pfeil links":
                return R.drawable.ic_remote_left;
            case "OK/Bestätigung":
                return R.drawable.ic_remote_ok;
        }
        return R.drawable.ic_remote;
    }

    //Fernbedienung abschließen
    private void finFB() {


        finFbDialog.show();
    }

    //Ende...
    private void end(boolean success) {

        if (success) {
            Log.d(TAG, "finished: saved");
            Toast.makeText(context, R.string.remote_configuration_success, Toast.LENGTH_SHORT).show();
            endDialog.setTitle(R.string.remote_configuration_end_title);
            endDialog.setMessage(context.getResources().getString(R.string.remote_configuration_end_message));
        } else {
            Log.d(TAG, "finished: failed");
            Toast.makeText(context, R.string.remote_configuration_failure, Toast.LENGTH_SHORT).show();
            endDialog.setTitle(R.string.remote_configuration_end_title_failure);
            endDialog.setMessage(context.getResources().getString(R.string.remote_configuration_end_message_failure));
        }
        endDialog.show();
    }

    public abstract void finished();
}
