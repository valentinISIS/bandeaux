package bandeau;
import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Classe utilitaire pour représenter la classe-association UML
 */
class ScenarioElement {

    Effect effect;
    int repeats;

    ScenarioElement(Effect e, int r) {
        effect = e;
        repeats = r;
    }
}
/**
 * Un scenario mémorise une liste d'effets, et le nombre de repetitions pour chaque effet
 * Un scenario sait se jouer sur un bandeau.
 */
public class Scenario {

    private final List<ScenarioElement> myElements = new CopyOnWriteArrayList<>();
    private final ReadWriteLock verrouScenario = new ReentrantReadWriteLock();
    private final ReadWriteLock verrouEffect = new ReentrantReadWriteLock();
    /**
     * Ajouter un effect au scenario.
     *
     * @param e l'effet à ajouter
     * @param repeats le nombre de répétitions pour cet effet
     */
    public void addEffect(Effect e, int repeats) {
        verrouScenario.writeLock().lock();
        try {
            myElements.add(new ScenarioElement(e, repeats));
        } finally {
            verrouScenario.writeLock().unlock();
        }
    }

    /**
     * Jouer ce scenario sur un bandeau
     *
     * @param b le bandeau ou s'afficher.
     */
    public void playOn(Bandeau b) {
        new Thread(() -> {
            synchronized (b){
                verrouScenario.readLock().lock();
                try {
                    for (ScenarioElement element : myElements) {
                        verrouEffect.readLock().lock();
                        try {
                            for (int repeats = 0; repeats < element.repeats; repeats++) {
                                element.effect.playOn(b);
                            }
                        }
                        finally {
                            verrouEffect.readLock().unlock();
                        }
                    }
                } finally {
                    verrouScenario.readLock().unlock();
                }
            }
        }).start();
    }
}
