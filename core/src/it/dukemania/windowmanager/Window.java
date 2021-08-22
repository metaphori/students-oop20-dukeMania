package it.dukemania.windowmanager;

import it.dukemania.Model.GameModel;

public interface Window {
    void create();
    void render();
    void dispose();
    void resize(int width, int height);
    void receiveData(GameModel data);
    void setWindowListener(SwitchWindowNotifier listener);
}
