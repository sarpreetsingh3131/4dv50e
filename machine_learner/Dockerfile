FROM python:3.7.1
RUN mkdir -p /opt/machine_learner
WORKDIR /opt/machine_learner
ADD . /opt/machine_learner
RUN pip3 install -r requirements.txt
CMD python3 online/app.py