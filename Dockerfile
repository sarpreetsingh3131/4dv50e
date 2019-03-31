FROM python:3.7.1
RUN mkdir -p /opt/ann-learner
WORKDIR /opt/ann-learner
ADD . /opt/ann-learner
RUN pip3 install -r requirements.txt
EXPOSE 5000
CMD python3 App.py