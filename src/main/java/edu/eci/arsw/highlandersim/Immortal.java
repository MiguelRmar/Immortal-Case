package edu.eci.arsw.highlandersim;

import edu.eci.arst.concprg.prodcons.Consumer;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Immortal extends Thread {

    private int health;
    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());

    
    
    


    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue) {
        super(name);
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue=defaultDamageValue;
    }
    public void run() {
        while (!ControlFrame.getTerminated() && this.health>0) {
            if (ControlFrame.getPause()) {
                ControlFrame.counterPause.getAndIncrement();
                //Revisa que sea el ultimo en pausarse y Despierta al ControlFrame
                if (ControlFrame.counterPause.get() == immortalsPopulation.size()) {
                    synchronized(ControlFrame.frame){
                        ControlFrame.frame.notify();
                    }
                }
                waiting();
            } else {
                Immortal im;
                int myIndex = immortalsPopulation.indexOf(this);
                int nextFighterIndex = r.nextInt(immortalsPopulation.size());
                //avoid self-fight
                if (nextFighterIndex == myIndex) {
                    nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
                }
                im = immortalsPopulation.get(nextFighterIndex);
                if(im.getHealth()>0){
                    this.fight(im);
                }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ControlFrame.counterPause.getAndIncrement();
    }
    
    public void waiting() {
        synchronized (immortalsPopulation) {
            try {
                //decremento los hilos que aun no estan en estado TERMINATED
                immortalsPopulation.wait();
                ControlFrame.counterPause.getAndDecrement();
            } catch (InterruptedException ex) {
                Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    } 
    public void fight(Immortal i2) {
        if(ControlFrame.getPause()){
            //aumento el cotador para saber cual es el ultimo hilo en acabar
            ControlFrame.counterPause.getAndIncrement();
            waiting();
        }
        if(!ControlFrame.getTerminated() && this.health>0){
            Immortal f1,f2;        
            if(immortalsPopulation.indexOf(i2)<immortalsPopulation.indexOf(this)){
                f1=this;
                f2=i2;
            }else{
                f1=i2;
                f2=this;
            }
            synchronized (f1) {
                synchronized (f2) {
                    if (i2.getHealth() > 0) {
                        i2.changeHealth(i2.getHealth() - defaultDamageValue);
                        this.health += defaultDamageValue;
                        System.out.println("Fight: " + this + " vs " + i2);
                        if (i2.getHealth()== 0) {
                            System.out.println(this + " says:" + i2 + " is already dead!");                      
                            System.out.println(this+" I KILL "+i2);
                        }
                    }
                }
            }
        }
    }

    public void changeHealth(int v) {
        health = v;
    }

    public int getHealth() {
        return health;
    }

    @Override
    public String toString() {
        return name + "[" + health + "]";
    }

}
