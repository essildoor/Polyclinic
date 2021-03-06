package com.exigen.server;

import com.exigen.entity.Doctor;
import com.exigen.entity.Patient;
import com.exigen.entity.Record;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.exigen.util.ProtocolCodes.*;

@SuppressWarnings("Unchecked")
public class ConnectionHandler implements Runnable {

    private Socket client;
    private DBManager dbManager;
    private ObjectOutputStream objOut;
    private ObjectInputStream objInp;
    private Logger logger;
    private boolean trigger;

    public ConnectionHandler(Socket client) throws IOException {
        this.client = client;
        dbManager = new DBManager();
        logger = ServerLogger.getInstance().getLogger();
        objOut = new ObjectOutputStream(client.getOutputStream());
        objInp = new ObjectInputStream(client.getInputStream());
        trigger = true;
    }

    @Override
    public void run() {
        try {
            System.out.println("Client handler started");
            int request;
            while (trigger) {
                request = objInp.readInt();
                sendResponse(request);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "client handler crashed. " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "client handler crashed. " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "client handler crashed. " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * sends response to client according its request
     *
     * @param request request from client
     * @throws IOException
     */
    private void sendResponse(int request) throws IOException, ClassNotFoundException, SQLException {
        objOut.writeInt(OK);
        objOut.flush();
        switch (request) {
            case STOP:
                stop();
                break;
            case REQUEST_ALL_LISTS: {
                ArrayList result = new ArrayList();
                result.add(dbManager.getPatientsList());
                result.add(dbManager.getDoctorsList());
                result.add(dbManager.getRecordsList());
                objOut.writeObject(result);
                objOut.flush();
                break;
            }
            case REQUEST_PATIENTS_LIST: {
                objOut.writeObject(dbManager.search((Patient) objInp.readObject()));
                objOut.flush();
                break;
            }
            case REQUEST_DOCTORS_LIST: {
                objOut.writeObject(dbManager.search((Doctor) objInp.readObject()));
                objOut.flush();
                break;
            }
            case REQUEST_RECORDS_LIST: {
                objOut.writeObject(dbManager.getRecordsList());
                objOut.flush();
                break;
            }
            case REQUEST_ADD_PATIENT: {
                if (dbManager.add((Patient) objInp.readObject())) {
                    objOut.writeInt(OK);
                } else {
                    objOut.writeInt(ERROR);
                }
                objOut.flush();
                break;
            }
            case REQUEST_ADD_DOCTOR: {
                if (dbManager.add((Doctor) objInp.readObject())) {
                    objOut.writeInt(OK);
                } else {
                    objOut.writeInt(ERROR);
                }
                objOut.flush();
                break;
            }
            case REQUEST_ADD_RECORD: {
                if (dbManager.add((Record) objInp.readObject())) {
                    objOut.writeInt(OK);
                } else {
                    objOut.writeInt(ERROR);
                }
                objOut.flush();
                break;
            }
            case REQUEST_EDIT_PATIENT: {
                break;
            }
            case REQUEST_EDIT_DOCTOR: {
                break;
            }
            case REQUEST_DELETE_PATIENT: {
                if (dbManager.delete((Patient) objInp.readObject())) {
                    objOut.writeInt(OK);
                } else {
                    objOut.writeInt(ERROR);
                }
                objOut.flush();
                break;
            }
            case REQUEST_DELETE_DOCTOR: {
                if (dbManager.delete((Doctor) objInp.readObject())) {
                    objOut.writeInt(OK);
                } else {
                    objOut.writeInt(ERROR);
                }
                objOut.flush();
                break;
            }
            case REQUEST_DELETE_RECORD: {
                if (dbManager.delete((Record) objInp.readObject())) {
                    objOut.writeInt(OK);
                } else {
                    objOut.writeInt(ERROR);
                }
                objOut.flush();
                break;
            }
            case REQUEST_SEARCH_PATIENT: {
                Patient param = (Patient) objInp.readObject();
                objOut.writeObject(dbManager.search(param));
                objOut.flush();
                break;
            }
            case REQUEST_SEARCH_DOCTOR: {
                System.out.println("directing param into dbmanager");
                objOut.writeObject(dbManager.search((Doctor) objInp.readObject()));
                objOut.flush();
                break;
            }
            case REQUEST_DOCTOR_SPECIALIZATION_LIST: {
                objOut.writeObject(dbManager.getDoctorsSpecializationList());
                objOut.flush();
                break;
            }
            default:
                System.out.println("unknown request param in sendResponse():" + request);
        }
    }

    /**
     * closes resources before exit
     *
     * @throws IOException
     */
    private void stop() throws IOException {
        System.out.println("Client has been disconnected\nHandler terminated");
        logger.log(Level.FINE, "Client has been disconnected\n" +
                "Handler terminated");
        objInp.close();
        objOut.close();
        client.close();
        trigger = false;
    }
}
